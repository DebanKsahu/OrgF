package com.github.orgf.promptscreen.domain.model

import androidx.compose.ui.text.AnnotatedString
import com.github.orgf.promptscreen.ui.state.PromptCardUiState
import com.github.orgf.utils.enums.toPromptCategoryOrUnknown

data class PromptCard(
    val promptId: Long,
    val promptText: AnnotatedString,
    val promptCategory: String,
    val isEnabled: Boolean,
)

fun PromptCard.toPromptCardUiState(): PromptCardUiState {
    val iconRes = this.promptCategory.toPromptCategoryOrUnknown().toIconRes()
    return PromptCardUiState(
        promptId = this.promptId,
        promptText = this.promptText,
        promptCategory = this.promptCategory,
        isEnabled = this.isEnabled,
        iconRes = iconRes
    )
}

fun List<PromptCard>.toPromptCardUiStateList(): List<PromptCardUiState> =
    map { promptCard -> promptCard.toPromptCardUiState() }
