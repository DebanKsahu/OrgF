package com.github.orgf.core.agent.prompt

import com.github.orgf.core.agent.models.PromptDetail
import com.github.orgf.core.agent.models.PromptUpdateDetail
import com.github.orgf.core.agent.tool.TextEmbedding
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.core.database.models.PromptClusterTable
import com.google.mediapipe.tasks.components.containers.Embedding

class PromptManager(
	private val appDatabase: AppDatabase,
	private val textEmbeddingTools: TextEmbedding
) {

	companion object {
		const val LAYER1_SIMILARITY_SCORE_THRESHOLD = 0.6
		const val LAYER2_SIMILARITY_SCORE_THRESHOLD = 0.8
	}

	suspend fun addPrompt(promptDetail: PromptDetail): Long {
		var categoryId = appDatabase.promptTableDao().insertPromptCategory(
			promptCategory = PromptCategoryTable(categoryName = promptDetail.category)
		)
		if (categoryId == -1L) {
			categoryId = appDatabase.promptTableDao()
				.getPromptCategoryByName(categoryName = promptDetail.category)?.id ?: -1L
		}

		val promptEmbedding = textEmbeddingTools.calculateEmbedding(promptDetail.prompt)
		if (promptEmbedding != null) {
			val clusterGroupLayerOne = appDatabase.promptTableDao()
				.getTopLevelPromptClustersByCategoryId(categoryId = categoryId)
			if (clusterGroupLayerOne.isEmpty()) {
				val newLayerThreeClusterId = createNewCluster(
					categoryId = categoryId,
					parentClusterId = null,
					prompt = promptDetail.prompt,
					promptEmbedding = promptEmbedding,
					destinationFolder = promptDetail.destinationFolder,
					clusterNumber = 1
				)
				return newLayerThreeClusterId
			} else {
				val (possibleLayerOneClusterCandidates, layerOneClusterCandidatesSimilarityScores) = getClusterCandidate(
					clusterList = clusterGroupLayerOne,
					promptEmbedding = promptEmbedding,
					similarityScoreThreshold = LAYER1_SIMILARITY_SCORE_THRESHOLD
				)
				if (possibleLayerOneClusterCandidates.isEmpty()) {
					val newLayerThreeClusterId = createNewCluster(
						categoryId = categoryId,
						parentClusterId = null,
						prompt = promptDetail.prompt,
						promptEmbedding = promptEmbedding,
						destinationFolder = promptDetail.destinationFolder,
						clusterNumber = 1
					)
					return newLayerThreeClusterId
				} else {
					val clusterGroupLayerTwo = mutableListOf<PromptClusterTable>()
					for (clusterId in possibleLayerOneClusterCandidates) {
						val childClusterList =
							appDatabase.promptTableDao().getPromptClustersByParentClusterId(
								parentClusterId = clusterId
							)
						clusterGroupLayerTwo.addAll(childClusterList)
					}

					val (possibleLayerTwoClusterCandidates, layerTwoClusterCandidatesSimilarityScores) = getClusterCandidate(
						clusterList = clusterGroupLayerOne,
						promptEmbedding = promptEmbedding,
						similarityScoreThreshold = LAYER2_SIMILARITY_SCORE_THRESHOLD
					)

					if (possibleLayerTwoClusterCandidates.isEmpty()) {
						val newLayerThreeClusterId = createNewCluster(
							categoryId = categoryId,
							parentClusterId = possibleLayerOneClusterCandidates[layerOneClusterCandidatesSimilarityScores.indexOf(
								layerOneClusterCandidatesSimilarityScores.max()
							)],
							prompt = promptDetail.prompt,
							promptEmbedding = promptEmbedding,
							destinationFolder = promptDetail.destinationFolder,
							clusterNumber = 2,
							changeVectorEmbeddingCentroid = true
						)
						return newLayerThreeClusterId
					} else {
						val finalLayerTwoClusterCandidateId =
							possibleLayerTwoClusterCandidates[layerTwoClusterCandidatesSimilarityScores.indexOf(
								layerTwoClusterCandidatesSimilarityScores.max()
							)]
						val newLayerThreeClusterId = createNewCluster(
							categoryId = categoryId,
							parentClusterId = finalLayerTwoClusterCandidateId,
							prompt = promptDetail.prompt,
							promptEmbedding = promptEmbedding,
							destinationFolder = promptDetail.destinationFolder,
							clusterNumber = 3,
							changeVectorEmbeddingCentroid = true
						)
						return newLayerThreeClusterId
					}
				}
			}
		} else {
			val errorMsg = "Prompt Embedding Is Null"
			throw error(message = errorMsg)
		}
	}

	fun updatePrompt(newPromptDetail: PromptUpdateDetail) {

	}

	private fun getClusterCandidate(
		clusterList: List<PromptClusterTable>,
		promptEmbedding: Embedding,
		similarityScoreThreshold: Double
	): Pair<List<Long>, List<Double>> {
		val possibleClusterCandidates = mutableListOf<Long>()
		val clusterCandidatesSimilarityScores = mutableListOf<Double>()
		for (cluster in clusterList) {
			if (cluster.id != null) {
				val similarityScore = textEmbeddingTools.compareEmbeddings(
					embedding1 = Embedding.create(cluster.vectorEmbedding, null, 0, null),
					embedding2 = promptEmbedding
				)
				if (similarityScore >= similarityScoreThreshold) {
					possibleClusterCandidates.add(cluster.id)
					clusterCandidatesSimilarityScores.add(similarityScore)
				}
			}
		}
		return Pair(possibleClusterCandidates, clusterCandidatesSimilarityScores)
	}

	private suspend fun createNewCluster(
		categoryId: Long,
		parentClusterId: Long?,
		prompt: String,
		promptEmbedding: Embedding,
		destinationFolder: String,
		clusterNumber: Int,
		changeVectorEmbeddingCentroid: Boolean = false
	): Long {
		when (clusterNumber) {
			3 if parentClusterId != null -> {
				val newLayerThreeCluster = PromptClusterTable(
					categoryId = categoryId,
					parentClusterId = parentClusterId,
					text = prompt,
					vectorEmbedding = promptEmbedding.floatEmbedding(),
					destinationFolder = destinationFolder
				)

				val newLayerThreeClusterId =
					appDatabase.promptTableDao()
						.insertPromptCluster(clusterData = newLayerThreeCluster)

				if (changeVectorEmbeddingCentroid) {
					val parentClusterDetail = appDatabase.promptTableDao()
						.getPromptClusterById(clusterId = parentClusterId)
					if (parentClusterDetail != null) {
						val newVectorEmbeddingCentroid = calculateNewVectorEmbeddingCentroid(
							oldVectorEmbeddingCentroid = parentClusterDetail.vectorEmbedding,
							newVectorEmbedding = promptEmbedding,
							clusterSize = parentClusterDetail.clusterSize + 1
						)
						appDatabase.promptTableDao().updateCluster(
							newClusterData = PromptClusterTable(
								id = parentClusterId,
								categoryId = parentClusterDetail.categoryId,
								parentClusterId = parentClusterDetail.parentClusterId,
								clusterSize = parentClusterDetail.clusterSize + 1,
								vectorEmbedding = newVectorEmbeddingCentroid
							)
						)
					}
				}
				return newLayerThreeClusterId
			}

			2 if parentClusterId != null -> {
				val newLayerTwoCluster = PromptClusterTable(
					categoryId = categoryId,
					parentClusterId = parentClusterId,
					vectorEmbedding = promptEmbedding.floatEmbedding()
				)

				val newLayerTwoClusterId =
					appDatabase.promptTableDao()
						.insertPromptCluster(clusterData = newLayerTwoCluster)

				if (changeVectorEmbeddingCentroid) {
					val parentClusterDetail = appDatabase.promptTableDao()
						.getPromptClusterById(clusterId = parentClusterId)
					if (parentClusterDetail != null) {
						val newVectorEmbeddingCentroid = calculateNewVectorEmbeddingCentroid(
							oldVectorEmbeddingCentroid = parentClusterDetail.vectorEmbedding,
							newVectorEmbedding = promptEmbedding,
							clusterSize = parentClusterDetail.clusterSize + 1
						)
						appDatabase.promptTableDao().updateCluster(
							newClusterData = PromptClusterTable(
								id = parentClusterId,
								categoryId = parentClusterDetail.categoryId,
								parentClusterId = null,
								clusterSize = parentClusterDetail.clusterSize + 1,
								vectorEmbedding = newVectorEmbeddingCentroid
							)
						)
					}
				}

				return createNewCluster(
					categoryId = categoryId,
					parentClusterId = newLayerTwoClusterId,
					prompt = prompt,
					promptEmbedding = promptEmbedding,
					destinationFolder = destinationFolder,
					clusterNumber = 3,
					changeVectorEmbeddingCentroid = changeVectorEmbeddingCentroid
				)
			}

			1 -> {
				val newLayerOneCluster = PromptClusterTable(
					categoryId = categoryId,
					parentClusterId = null,
					vectorEmbedding = promptEmbedding.floatEmbedding()
				)

				val newLayerOneClusterId =
					appDatabase.promptTableDao()
						.insertPromptCluster(clusterData = newLayerOneCluster)

				return createNewCluster(
					categoryId = categoryId,
					parentClusterId = newLayerOneClusterId,
					prompt = prompt,
					promptEmbedding = promptEmbedding,
					destinationFolder = destinationFolder,
					clusterNumber = 2
				)
			}

			else -> {
				val errorMsg = "No Matching Operation As Per Given Parameters"
				error(message = errorMsg)
			}
		}
	}

	fun calculateNewVectorEmbeddingCentroid(
		oldVectorEmbeddingCentroid: FloatArray, newVectorEmbedding: Embedding, clusterSize: Int
	): FloatArray {
		val newVectorEmbeddingCentroid = FloatArray(size = oldVectorEmbeddingCentroid.size)
		val newVectorEmbeddingFloat = newVectorEmbedding.floatEmbedding()
		for (index in oldVectorEmbeddingCentroid.indices) {
			val newValue =
				((oldVectorEmbeddingCentroid[index] * clusterSize) + newVectorEmbeddingFloat[index]) / (clusterSize + 1)
			newVectorEmbeddingCentroid[index] = newValue
		}
		return newVectorEmbeddingCentroid
	}
}


