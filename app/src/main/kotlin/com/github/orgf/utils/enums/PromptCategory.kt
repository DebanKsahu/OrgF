package com.github.orgf.utils.enums

import com.github.orgf.R

enum class PromptCategory {
    ImageType,
    VideoType,
    AudioType,
    PdfType,
    WordType,
    ExcelType,
    PowerPointType,
    TextType,
    ArchiveType,
    UnknownType;

    fun toIconRes(): Int {
        return when (this) {
            ImageType -> R.drawable.ic_image
            VideoType -> R.drawable.ic_video
            AudioType -> R.drawable.ic_audio
            UnknownType -> R.drawable.ic_folder
            PdfType -> R.drawable.ic_document
            WordType -> R.drawable.ic_document
            ExcelType -> R.drawable.ic_document
            PowerPointType -> R.drawable.ic_document
            TextType -> R.drawable.ic_document
            ArchiveType -> R.drawable.ic_folder
        }
    }
}

fun String.toPromptCategoryOrUnknown(): PromptCategory =
    PromptCategory.entries.firstOrNull { category -> category.name.equals(this, ignoreCase = true) }
        ?: PromptCategory.UnknownType