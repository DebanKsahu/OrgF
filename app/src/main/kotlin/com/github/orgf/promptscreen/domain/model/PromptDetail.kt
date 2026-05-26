package com.github.orgf.promptscreen.domain.model

import com.github.orgf.utils.enums.PromptCategory

data class PromptDetail(
    val prompt: String,
    val category: PromptCategory,
    val destinationFolder: String
)
