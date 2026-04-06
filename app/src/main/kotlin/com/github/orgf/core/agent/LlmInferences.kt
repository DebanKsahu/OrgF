package com.github.orgf.core.agent

import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LlmInferences {
    private val smallLlmModelPath = "/data/local/tmp/llm/gemma/gemma3-1b-it-int4.task"

    val smallLlmSmallTokenTaskOptions = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(smallLlmModelPath)
        .setMaxTopK(64)
        .setMaxTokens(512)
        .build()

    val smallLlmMediumTokenTaskOptions = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(smallLlmModelPath)
        .setMaxTopK(64)
        .setMaxTokens(1024)
        .build()

    val smallLlmLargeTokenTaskOptions = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(smallLlmModelPath)
        .setMaxTopK(64)
        .setMaxTokens(2048)
        .build()
}