package com.github.orgf.promptscreen.ui.mapper

import com.github.orgf.R
import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.ui.state.PromptCardUiState
import com.github.orgf.utils.enums.PromptCategory

fun PromptCard.toPromptCardUiState(): PromptCardUiState {
    val iconRes = promptCategory.toPromptCategoryOrUnknown().toIconRes()
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

private fun String.toPromptCategoryOrUnknown(): PromptCategory =
    PromptCategory.entries.firstOrNull { category -> category.name.equals(this, ignoreCase = true) }
        ?: PromptCategory.UnknownType

private fun PromptCategory.toIconRes(): Int = when (this) {
    PromptCategory.ImageType -> R.drawable.ic_image
    PromptCategory.DocumentType -> R.drawable.ic_document
    PromptCategory.VideoType -> R.drawable.ic_video
    PromptCategory.AudioType -> R.drawable.ic_audio
    PromptCategory.UnknownType -> R.drawable.ic_folder
}