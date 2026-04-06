package com.github.orgf.promptscreen.ui.state

import com.github.orgf.utils.enums.PromptCategory

data class NewPromptUiState(
    val prompt: String = "",
    val category: PromptCategory = PromptCategory.ImageType,
    val destinationFolder: String = ""
)
