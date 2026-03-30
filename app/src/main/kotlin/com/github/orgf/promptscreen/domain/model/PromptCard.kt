package com.github.orgf.promptscreen.domain.model

import androidx.compose.ui.text.AnnotatedString

data class PromptCard(
    val promptId: Long,
    val promptText: AnnotatedString,
    val promptCategory: String,
    val isEnabled: Boolean,
)
