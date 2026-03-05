package com.github.orgf.core.agent.tool

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.Embedding
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder

class TextEmbedding(
    private val platformContext: Context,
    val currentModel: Int = DELEGATE_CPU,
    val currentDelegate: Int = 0
) {
    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1

        const val UNIVERSAL_SENTENCE_ENCODER = 0
        const val SENTENCE_TRANSFORMER = 1

        const val UNIVERSAL_SENTENCE_ENCODER_PATH = "textembedding/universal_sentence_encoder.tflite"
        const val SENTENCE_TRANSFORMER_PATH = "textembedding/sentence_transformer.tflite"

        const val LOG_TAG = "TextEmbedding"
    }

    private var textEmbedder: TextEmbedder? = null

    init {
        setupTextEmbedder()
    }

    fun setupTextEmbedder() {
        val baseOptionBuilder = BaseOptions.builder()

        when (currentDelegate) {
            DELEGATE_GPU -> baseOptionBuilder.setDelegate(Delegate.GPU)
            DELEGATE_CPU -> baseOptionBuilder.setDelegate(Delegate.CPU)
        }

        when (currentModel) {
            UNIVERSAL_SENTENCE_ENCODER -> baseOptionBuilder.setModelAssetPath(UNIVERSAL_SENTENCE_ENCODER_PATH)
            SENTENCE_TRANSFORMER -> baseOptionBuilder.setModelAssetPath(SENTENCE_TRANSFORMER_PATH)
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            val options = TextEmbedder.TextEmbedderOptions
                .builder()
                .setBaseOptions(baseOptions)
                .build()

            textEmbedder = TextEmbedder.createFromOptions(platformContext, options)
        } catch (e: IllegalStateException) {
            Log.d(LOG_TAG, "Text embedder failed to load model with error: ${e.message}")
        } catch (e: RuntimeException) {
            Log.d(LOG_TAG, "Text embedder failed to load model with error: ${e.message}")
        }
    }

    fun compareText(text1: String, text2: String): Double? {
        if (textEmbedder != null) {
            val firstTextEmbedding = calculateEmbedding(text = text1)
            val secondTextEmbedding = calculateEmbedding(text = text2)

            return TextEmbedder.cosineSimilarity(firstTextEmbedding, secondTextEmbedding)
        } else {
            Log.d(LOG_TAG, "Text Embedder is not initialized")
            return null
        }
    }

    fun compareEmbeddings(embedding1: Embedding, embedding2: Embedding): Double {
        return TextEmbedder.cosineSimilarity(embedding1, embedding2)
    }

    fun calculateEmbedding(text: String): Embedding? {
        return if (textEmbedder != null) {
            textEmbedder
                ?.embed(text)
                ?.embeddingResult()
                ?.embeddings()
                ?.first()
        } else {
            Log.d(LOG_TAG, "Text Embedder is not initialized")
            null
        }
    }

    fun clearTextEmbedder() {
        textEmbedder?.close()
        textEmbedder = null
    }
}