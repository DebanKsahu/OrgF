package com.github.orgf.utils.enums

import com.github.orgf.R

enum class PromptCategory {
    ImageType,
    VideoType,
    DocumentType,
    AudioType,
    UnknownType;

    fun toIconRes(): Int {
        return when (this) {
            ImageType -> R.drawable.ic_image
            DocumentType -> R.drawable.ic_document
            VideoType -> R.drawable.ic_video
            AudioType -> R.drawable.ic_audio
            UnknownType -> R.drawable.ic_folder
        }
    }
}

fun String.toPromptCategoryOrUnknown(): PromptCategory =
    PromptCategory.entries.firstOrNull { category -> category.name.equals(this, ignoreCase = true) }
        ?: PromptCategory.UnknownType