package com.github.orgf.promptscreen.domain.model

import com.github.orgf.utils.enums.PromptCategory

data class PromptDetailDomain(
    val prompt: String,
    val category: PromptCategory,
    val destinationFolder: String
)
