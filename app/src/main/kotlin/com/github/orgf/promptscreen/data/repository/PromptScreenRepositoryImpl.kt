package com.github.orgf.promptscreen.data.repository

import com.github.orgf.core.agent.tool.EmbeddingTypes
import com.github.orgf.core.agent.tool.TextEmbedding
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.models.PromptClusterTable
import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.domain.model.PromptDetail
import com.github.orgf.promptscreen.domain.model.toPromptCardList
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.utils.enums.PromptCategory
import com.google.mediapipe.tasks.components.containers.Embedding

class PromptScreenRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val textEmbeddingTools: TextEmbedding
) : PromptScreenRepository {

    companion object {
        private const val LAYER1_SIMILARITY_SCORE_THRESHOLD = 0.4
        private const val LAYER2_SIMILARITY_SCORE_THRESHOLD = 0.4
    }

    override suspend fun getAllPromptDetail(): List<PromptCard> {
        return appDatabase.promptTableDao().getAllPromptDetail()
            .toPromptCardList(appDatabase = appDatabase)
    }

    override suspend fun getPromptCategoryDetailById(categoryId: Long): PromptCategory {
        return try {
            val promptCategoryDetail =
                appDatabase.promptTableDao().getPromptCategoryDetailById(categoryId)
            promptCategoryDetail?.categoryName
                ?: error("There is no prompt category with id: $categoryId")
        } catch (e: Exception) {
            error("Failed to get prompt category with id: $categoryId. Error: ${e.message}")
        }
    }

    override suspend fun getPromptDetailByCategoryName(category: PromptCategory): List<PromptCard> {
        val result = appDatabase.promptTableDao()
            .getPromptDetailByCategoryName(categoryName = category)
            .toPromptCardList(appDatabase = appDatabase)
        return result
    }

    override suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean) {
        appDatabase.promptTableDao().updatePromptActiveStatus(promptId, isActive)
    }

    override suspend fun addPromptDetail(promptDetail: PromptDetail): Long {
        val categoryId = appDatabase.promptTableDao()
            .getOrInsertPromptCategoryIdByName(categoryName = promptDetail.category)

        val promptEmbedding =
            textEmbeddingTools.calculateEmbedding(promptDetail.prompt) ?: error("Some Error")

        val layerOnePromptClusterList = appDatabase.promptTableDao()
            .getLayer1PromptClusterDetailByCategoryId(categoryId = categoryId)
        if (layerOnePromptClusterList.isEmpty()) {
            return createNewPromptCluster(
                categoryId = categoryId,
                parentClusterId = null,
                prompt = promptDetail.prompt,
                promptEmbedding = promptEmbedding,
                destinationFolder = promptDetail.destinationFolder,
                startLayer = 1
            )
        }

        val selectedLayerOnePromptClusterList = selectPromptCluster(
            promptClusterList = layerOnePromptClusterList,
            promptEmbedding = promptEmbedding,
            similarityScoreThreshold = LAYER1_SIMILARITY_SCORE_THRESHOLD
        )

        if (selectedLayerOnePromptClusterList.isEmpty()) {
            return createNewPromptCluster(
                categoryId = categoryId,
                parentClusterId = null,
                prompt = promptDetail.prompt,
                promptEmbedding = promptEmbedding,
                destinationFolder = promptDetail.destinationFolder,
                startLayer = 1
            )
        }

        val selectedLayerTwoPromptClusterList = mutableListOf<Pair<Long, Double>>()
        for ((id, _) in selectedLayerOnePromptClusterList) {
            val clusterList = appDatabase.promptTableDao()
                .getPromptClusterDetailByParentClusterId(parentClusterId = id)

            val (selectedClusterIdList, _) = selectPromptCluster(
                promptClusterList = clusterList,
                promptEmbedding = promptEmbedding,
                similarityScoreThreshold = LAYER2_SIMILARITY_SCORE_THRESHOLD
            )

            selectedLayerTwoPromptClusterList += selectedClusterIdList
        }

        if (selectedLayerTwoPromptClusterList.isEmpty()) {
            return createNewPromptCluster(
                categoryId = categoryId,
                parentClusterId = selectedLayerOnePromptClusterList[0].first,
                prompt = promptDetail.prompt,
                promptEmbedding = promptEmbedding,
                destinationFolder = promptDetail.destinationFolder,
                startLayer = 2,
                willCentroidChange = true
            )
        }

        return createNewPromptCluster(
            categoryId = categoryId,
            parentClusterId = selectedLayerTwoPromptClusterList[0].first,
            prompt = promptDetail.prompt,
            promptEmbedding = promptEmbedding,
            destinationFolder = promptDetail.destinationFolder,
            startLayer = 3,
            willCentroidChange = true
        )
    }

    private fun calculateClusterCentroid(
        oldCentroid: FloatArray,
        oldFloatEmbedding: FloatArray,
        newEmbedding: Embedding,
        oldClusterSize: Int,
        newClusterSize: Int
    ): FloatArray {
        val newCentroid = FloatArray(oldCentroid.size)
        val newFloatEmbedding = newEmbedding.floatEmbedding() ?: error("Some Error")

        if (newClusterSize == oldClusterSize) {
            for (index in newCentroid.indices) {
                val newCentroidValue =
                    oldCentroid[index] + ((newFloatEmbedding[index] - oldFloatEmbedding[index]) / oldClusterSize)
                newCentroid[index] = newCentroidValue
            }
        } else {
            for (index in newCentroid.indices) {
                val newCentroidValue =
                    ((oldCentroid[index] * oldClusterSize) + newFloatEmbedding[index]) / newClusterSize
                newCentroid[index] = newCentroidValue
            }
        }

        return newCentroid
    }

    private fun calculateClusterCentroid(
        oldCentroid: FloatArray,
        oldFloatEmbedding: FloatArray,
        newFloatEmbedding: FloatArray,
        oldClusterSize: Int,
        newClusterSize: Int
    ): FloatArray {
        val newCentroid = FloatArray(oldCentroid.size)

        if (newClusterSize == oldClusterSize) {
            for (index in newCentroid.indices) {
                val newCentroidValue =
                    oldCentroid[index] + ((newFloatEmbedding[index] - oldFloatEmbedding[index]) / oldClusterSize)
                newCentroid[index] = newCentroidValue
            }
        } else {
            for (index in newCentroid.indices) {
                val newCentroidValue =
                    ((oldCentroid[index] * oldClusterSize) + newFloatEmbedding[index]) / newClusterSize
                newCentroid[index] = newCentroidValue
            }
        }

        return newCentroid
    }

    private suspend fun updateParentClusterCentroid(
        parentClusterId: Long,
        oldFloatEmbedding: FloatArray,
        currEmbedding: Embedding,
        isNewClusterAdded: Boolean = true
    ) {
        var currParentClusterId: Long? = parentClusterId
        var currFloatEmbedding = currEmbedding.floatEmbedding() ?: error("Some Error")
        var oldFloatEmbedding = oldFloatEmbedding
        var isNewClusterAdded = isNewClusterAdded

        while (currParentClusterId != null) {
            val parentClusterDetail = appDatabase.promptTableDao()
                .getPromptClusterDetailById(clusterId = currParentClusterId)

            if (parentClusterDetail == null) error("Some Error")

            var newClusterSize = parentClusterDetail.clusterSize
            if (isNewClusterAdded) {
                newClusterSize += 1
                isNewClusterAdded = false
            }

            val newCentroid = calculateClusterCentroid(
                oldCentroid = parentClusterDetail.vectorEmbedding,
                oldFloatEmbedding = oldFloatEmbedding,
                newFloatEmbedding = currFloatEmbedding,
                oldClusterSize = parentClusterDetail.clusterSize,
                newClusterSize = newClusterSize
            )

            oldFloatEmbedding = parentClusterDetail.vectorEmbedding

            appDatabase.promptTableDao()
                .updatePromptClusterDetail(
                    newClusterData = parentClusterDetail.copy(
                        id = currParentClusterId,
                        clusterSize = newClusterSize,
                        vectorEmbedding = newCentroid,
                    )
                )

            currFloatEmbedding = newCentroid
            currParentClusterId = parentClusterDetail.parentClusterId
        }
    }

    private suspend fun updateParentClusterCentroid(
        parentClusterId: Long,
        oldFloatEmbedding: FloatArray,
        currFloatEmbedding: FloatArray,
        isNewClusterAdded: Boolean = true
    ) {
        var currParentClusterId: Long? = parentClusterId
        var currFloatEmbedding = currFloatEmbedding
        var oldFloatEmbedding = oldFloatEmbedding
        var isNewClusterAdded = isNewClusterAdded

        while (currParentClusterId != null) {
            val parentClusterDetail = appDatabase.promptTableDao()
                .getPromptClusterDetailById(clusterId = currParentClusterId)

            if (parentClusterDetail == null) error("Some Error")

            var newClusterSize = parentClusterDetail.clusterSize
            if (isNewClusterAdded) {
                newClusterSize += 1
                isNewClusterAdded = false
            }

            val newCentroid = calculateClusterCentroid(
                oldCentroid = parentClusterDetail.vectorEmbedding,
                oldFloatEmbedding = oldFloatEmbedding,
                newFloatEmbedding = currFloatEmbedding,
                oldClusterSize = parentClusterDetail.clusterSize,
                newClusterSize = newClusterSize
            )

            oldFloatEmbedding = parentClusterDetail.vectorEmbedding

            appDatabase.promptTableDao()
                .updatePromptClusterDetail(
                    newClusterData = parentClusterDetail.copy(
                        id = currParentClusterId,
                        clusterSize = newClusterSize,
                        vectorEmbedding = newCentroid,
                    )
                )

            currFloatEmbedding = newCentroid
            currParentClusterId = parentClusterDetail.parentClusterId
        }
    }

    private fun selectPromptCluster(
        promptClusterList: List<PromptClusterTable>,
        promptEmbedding: Embedding,
        similarityScoreThreshold: Double
    ): MutableList<Pair<Long, Double>> {
        val finalPromptClusterList = mutableListOf<Pair<Long, Double>>()

        for (cluster in promptClusterList) {
            if (cluster.id == null) continue
            val similarityScore = textEmbeddingTools.compareEmbeddings(
                embedding1 = EmbeddingTypes.FloatArrayType(cluster.vectorEmbedding),
                embedding2 = EmbeddingTypes.EmbeddingType(promptEmbedding)
            )
            if (similarityScore >= similarityScoreThreshold) {
                finalPromptClusterList.add(Pair(cluster.id, similarityScore))
            }
        }

        finalPromptClusterList.sortBy { -it.second }

        return finalPromptClusterList
    }

    private suspend fun createNewPromptCluster(
        categoryId: Long,
        parentClusterId: Long?,
        prompt: String,
        promptEmbedding: Embedding,
        destinationFolder: String,
        startLayer: Int,
        willCentroidChange: Boolean = false
    ): Long {
        when (startLayer) {
            3 if parentClusterId != null -> {
                val newCLuster = PromptClusterTable(
                    categoryId = categoryId,
                    parentClusterId = parentClusterId,
                    text = prompt,
                    vectorEmbedding = promptEmbedding.floatEmbedding(),
                    destinationFolder = destinationFolder
                )

                val newCLusterId = appDatabase.promptTableDao()
                    .insertPromptClusterDetail(clusterData = newCLuster)

                if (willCentroidChange) {
                    updateParentClusterCentroid(
                        parentClusterId = parentClusterId,
                        oldFloatEmbedding = FloatArray(size = 0),
                        currFloatEmbedding = promptEmbedding.floatEmbedding()
                            ?: error("Some Error"),
                        isNewClusterAdded = true
                    )
                }

                return newCLusterId
            }

            2 if parentClusterId != null -> {
                val newCluster = PromptClusterTable(
                    categoryId = categoryId,
                    parentClusterId = parentClusterId,
                    vectorEmbedding = promptEmbedding.floatEmbedding()
                )

                val newClusterId = appDatabase.promptTableDao()
                    .insertPromptClusterDetail(clusterData = newCluster)

                if (willCentroidChange) {
                    updateParentClusterCentroid(
                        parentClusterId = parentClusterId,
                        oldFloatEmbedding = FloatArray(size = 0),
                        currFloatEmbedding = promptEmbedding.floatEmbedding()
                            ?: error("Some Error"),
                        isNewClusterAdded = true
                    )
                }

                return createNewPromptCluster(
                    categoryId = categoryId,
                    parentClusterId = newClusterId,
                    prompt = prompt,
                    promptEmbedding = promptEmbedding,
                    destinationFolder = destinationFolder,
                    startLayer = 3,
                )
            }

            1 -> {
                val newCluster = PromptClusterTable(
                    categoryId = categoryId,
                    parentClusterId = parentClusterId,
                    vectorEmbedding = promptEmbedding.floatEmbedding()
                )

                val newClusterId = appDatabase.promptTableDao()
                    .insertPromptClusterDetail(clusterData = newCluster)

                return createNewPromptCluster(
                    categoryId = categoryId,
                    parentClusterId = newClusterId,
                    prompt = prompt,
                    promptEmbedding = promptEmbedding,
                    destinationFolder = destinationFolder,
                    startLayer = 2,
                )
            }

            else -> {
                val errorMsg = "No Matching Operation As Per Given Parameters"
                error(message = errorMsg)
            }
        }
    }
}