package com.github.orgf.promptscreen.ui.state

import androidx.compose.ui.text.AnnotatedString

data class PromptCardUiState(
    val promptId: Long,
    val promptText: AnnotatedString,
    val promptCategory: String,
    val isEnabled: Boolean,
    val iconRes: Int,
)
