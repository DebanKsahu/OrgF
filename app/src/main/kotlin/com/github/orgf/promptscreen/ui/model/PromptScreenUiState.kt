package com.github.orgf.promptscreen.ui.model

data class PromptScreenUiState(
    val isLoading: Boolean = false,
    val promptList: List<PromptCardUiState>? = null,
    val error: String? = null
)
