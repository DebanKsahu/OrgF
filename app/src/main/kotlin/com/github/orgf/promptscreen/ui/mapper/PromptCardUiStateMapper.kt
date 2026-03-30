package com.github.orgf.promptscreen.ui.mapper

import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.ui.state.PromptCardUiState

fun PromptCard.toPromptCardUiState(): PromptCardUiState {
    val iconRes = 1
    return PromptCardUiState(
        promptId = this.promptId,
        promptText = this.promptText,
        promptCategory = this.promptCategory,
        isEnabled = this.isEnabled,
        iconRes = iconRes
    )
}

fun List<PromptCard>.toPromptCardUiStateList(): List<PromptCardUiState> {
    val resultList = mutableListOf<PromptCardUiState>()
    for (promptCard in this) {
        resultList.add(promptCard.toPromptCardUiState())
    }
    return resultList.toList()
}