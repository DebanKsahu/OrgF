package com.github.orgf.promptscreen.ui.model

import androidx.compose.ui.text.AnnotatedString
import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.utils.enums.toPromptCategoryOrUnknown

data class PromptCardUiState(
    val promptId: Long,
    val promptText: AnnotatedString,
    val promptCategory: String,
    val isEnabled: Boolean,
    val iconRes: Int,
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