package com.github.orgf.core.agent.tool

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.models.PromptClusterTable
import com.github.orgf.utils.enums.FileType
import com.github.orgf.utils.enums.PromptCategory
import com.github.orgf.utils.enums.toPromptCategory
import java.io.File

class FileOrganizer(
    private val pdfTextExtractor: PdfTextExtractor,
    private val textEmbedding: TextEmbedding,
    private val appDatabase: AppDatabase,
    private val applicationContext: Context
) {

    companion object {
        const val LAYER1_SIMILARITY_SCORE_THRESHOLD = 0.4
        const val LAYER2_SIMILARITY_SCORE_THRESHOLD = 0.4
        const val LAYER3_SIMILARITY_SCORE_THRESHOLD = 0.4

        const val DEFAULT_DESTINATION_FOLDER = "unorganizedFiles"

        const val TAG = "FileOrganizer"
    }

    /**
     * This function will organize the file
     *
     * @param fullFilePath This the path of the file
     * @param fileName This is the name of the file
     * @param fileType This is the type of the file
     */
    suspend fun organizeFile(
        fullFilePath: String,
        fileName: String,
        fileType: FileType,
        rootDoc: DocumentFile,
        rootFolderUri: Uri
    ) {
        Log.d(TAG, "▶ Starting organization for: $fileName (Type: $fileType)")
        Log.d(TAG, "Root URI: $rootFolderUri")

        val fileTextContent = when (fileType) {
            FileType.ImageType -> TODO()
            FileType.VideoType -> TODO()
            FileType.AudioType -> TODO()
            FileType.UnknownType -> TODO()
            FileType.PdfType -> {
                val pdfFile = File(fullFilePath)
                pdfTextExtractor.extractSmallText(pdfFile = pdfFile)
            }

            FileType.WordType -> TODO()
            FileType.ExcelType -> TODO()
            FileType.PowerPointType -> TODO()
            FileType.TextType -> TODO()
            FileType.ArchiveType -> TODO()
        }

        val targetPromptCluster = this.findMatchingPromptCluster(
            fileTextContent = fileTextContent,
            targetCategory = fileType.toPromptCategory()
        )

        Log.d(TAG, "Matching Cluster: ${targetPromptCluster ?: "NONE (Using Default)"}")

        val sourceFileDoc = rootDoc.findFile(fileName) ?: return
        val sourceFileUri = sourceFileDoc.uri
        val sourceFileParentUri = rootFolderUri
        val sourceFileParentDocUri = DocumentsContract.buildDocumentUriUsingTree(
            sourceFileParentUri,
            DocumentsContract.getTreeDocumentId(sourceFileParentUri)
        )

        val destFolderPath =
            if (targetPromptCluster != null && targetPromptCluster.destinationFolder != null) {
                targetPromptCluster.destinationFolder
            } else {
                DEFAULT_DESTINATION_FOLDER
            }
        Log.d(TAG, "Navigating to destination: $destFolderPath")
        val destFolderDoc = this.getOrCreateSubFolder(
            rootDoc = rootDoc,
            path = destFolderPath
        )

        if (destFolderDoc != null) {
            val destFolderDocUri = destFolderDoc.uri

            if (destFolderDoc.uri == rootFolderUri) {
                Log.i(TAG, "File is already in the target folder. Skipping.")
                return
            }

            destFolderDoc.findFile(fileName)?.let {
                Log.w(
                    TAG,
                    "Conflict found: $fileName already exists in $destFolderPath. Deleting old file."
                )
                it.delete()
            }

            val success = moveFileToDestination(
                applicationContext = applicationContext,
                sourceFileUri = sourceFileUri,
                sourceFileParentUri = sourceFileParentDocUri,
                destFolderUri = destFolderDocUri
            )

            if (success) {
                Log.d("Storage", "Successfully moved $fileName to $destFolderPath")
            }
        }
    }

    /**
     * This function will find the matching prompt for the particular file
     * which is being processed to be organized.
     *
     * @param fileTextContent The text content extracted from the file (Regardless of the file type)
     */
    private suspend fun findMatchingPromptCluster(
        fileTextContent: String,
        targetCategory: PromptCategory
    ): PromptClusterTable? {
        Log.d(TAG, "Calculating embedding for category: $targetCategory")
        val fileTextEmbedding = textEmbedding.calculateEmbedding(text = fileTextContent)
        if (fileTextEmbedding != null) {
            val categoryId = appDatabase.promptTableDao()
                .getOrInsertPromptCategoryIdByName(categoryName = targetCategory)
            val availableLayer1Candidates = mutableListOf<PromptClusterTable>()
            val availableLayer2Candidates = mutableListOf<PromptClusterTable>()

            // Iterate through layer 1 clusters to find layer2 cluster candidates
            for (promptCluster in appDatabase.promptTableDao()
                .getLayer1PromptClusterDetailByCategoryId(categoryId = categoryId)) {
                val similarityScore = textEmbedding.compareEmbeddings(
                    embedding1 = EmbeddingTypes.FloatArrayType(promptCluster.vectorEmbedding),
                    embedding2 = EmbeddingTypes.EmbeddingType(fileTextEmbedding)
                )

                if (similarityScore >= LAYER1_SIMILARITY_SCORE_THRESHOLD) {
                    Log.d(
                        "FileOrganizer",
                        "Checking Cluster Layer 1: ${promptCluster.id} | Score: $similarityScore"
                    )
                    availableLayer1Candidates.add(promptCluster)
                }
            }

            Log.d(TAG, "Passed Layer 1: ${availableLayer1Candidates.size} clusters.")

            // Iterate through layer 2 clusters to find layer3 cluster candidates
            for (promptCluster in availableLayer1Candidates) {
                for (subPromptCluster in appDatabase.promptTableDao()
                    .getPromptClusterDetailByParentClusterId(promptCluster.id)) {
                    val similarityScore = textEmbedding.compareEmbeddings(
                        embedding1 = EmbeddingTypes.FloatArrayType(subPromptCluster.vectorEmbedding),
                        embedding2 = EmbeddingTypes.EmbeddingType(fileTextEmbedding)
                    )
                    if (similarityScore >= LAYER2_SIMILARITY_SCORE_THRESHOLD) {
                        Log.d(
                            "FileOrganizer",
                            "Checking Cluster Layer 2: ${subPromptCluster.id} | Score: $similarityScore"
                        )
                        availableLayer2Candidates.add(subPromptCluster)
                    }
                }
            }

            Log.d(TAG, "Passed Layer 2: ${availableLayer2Candidates.size} clusters.")

            // Find the best matching cluster among layer 3 candidates
            var currentBestSimilarityScore = 0.0
            var currentBestPromptCluster: PromptClusterTable? = null
            for (promptCluster in availableLayer2Candidates) {
                for (subPromptCluster in appDatabase.promptTableDao()
                    .getPromptClusterDetailByParentClusterId(promptCluster.id)) {
                    val similarityScore = textEmbedding.compareEmbeddings(
                        embedding1 = EmbeddingTypes.FloatArrayType(subPromptCluster.vectorEmbedding),
                        embedding2 = EmbeddingTypes.EmbeddingType(fileTextEmbedding)
                    )
                    if (similarityScore >= LAYER3_SIMILARITY_SCORE_THRESHOLD) {
                        Log.d(
                            "FileOrganizer",
                            "Checking Cluster Layer 3: ${subPromptCluster.id} | Score: $similarityScore"
                        )
                        if (similarityScore > currentBestSimilarityScore) {
                            currentBestPromptCluster = subPromptCluster
                            currentBestSimilarityScore = similarityScore
                        }
                    }
                }
            }

            return currentBestPromptCluster

        } else {
            Log.e(TAG, "Embedding calculation returned NULL")
            throw IllegalStateException("Failed to calculate embedding for the file content.")
        }
    }

    /**
     * This function will move the file to the destination folder
     *
     * @param destFolderUri The relative path to the destination folder
     */
    private fun moveFileToDestination(
        applicationContext: Context,
        sourceFileUri: Uri,
        sourceFileParentUri: Uri,
        destFolderUri: Uri
    ): Boolean {
        Log.d(TAG, "Executing moveDocument...")
        Log.d(TAG, " - Source File: $sourceFileUri")
        Log.d(TAG, " - From Parent: $sourceFileParentUri")
        Log.d(TAG, " - To Folder:   $destFolderUri")

        return try {
            val resultUri = DocumentsContract.moveDocument(
                applicationContext.contentResolver,
                sourceFileUri,
                sourceFileParentUri,
                destFolderUri
            )
            resultUri != null
        } catch (e: Exception) {
            Log.e("MoveError", "Failed to move: ${e.message}")
            false
        }
    }

    private fun getOrCreateSubFolder(rootDoc: DocumentFile, path: String): DocumentFile? {
        var current = rootDoc
        val segments = path.split("/").filter { it.isNotEmpty() }
        Log.d(TAG, "Walking path segments: $segments")

        for (segment in segments) {
            val found = current.findFile(segment)
            current = if (found == null || !found.isDirectory) {
                Log.d(TAG, "Creating directory: $segment")
                current.createDirectory(segment) ?: return null
            } else {
                found
            }
        }
        return current
    }
}