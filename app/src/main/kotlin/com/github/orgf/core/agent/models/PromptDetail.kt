package com.github.orgf.core.agent.models

import com.github.orgf.utils.enums.PromptCategory

data class PromptDetail(
    val category: PromptCategory,
    val prompt: String,
    val destinationFolder: String
)
