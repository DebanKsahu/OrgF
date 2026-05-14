package com.github.orgf.utils.enums

import android.webkit.MimeTypeMap
import java.io.File
import java.util.Locale

enum class FileType {
    ImageType,
    VideoType,
    AudioType,
    PdfType,
    WordType,
    ExcelType,
    PowerPointType,
    TextType,
    ArchiveType,
    UnknownType
}

/**
 * This function fetches file type from the **name of the file**
 * @param fileName The name of the file
 * @return Type of the file (**FileType**)
 */
fun getFileType(fileName: String): FileType {
    val extension = File(fileName).extension.lowercase(Locale.ROOT)
    val mimeType = MimeTypeMap.getSingleton()?.getMimeTypeFromExtension(extension) ?: ""

    return when {
        extension == "pdf" -> FileType.PdfType
        extension in listOf("doc", "docx") -> FileType.WordType
        extension in listOf("xls", "xlsx") -> FileType.ExcelType
        extension in listOf("ppt", "pptx") -> FileType.PowerPointType
        extension in listOf("txt", "md", "markdown") -> FileType.TextType
        extension in listOf("zip", "rar", "7z", "tar") -> FileType.ArchiveType
        mimeType.startsWith("image/") -> FileType.ImageType
        mimeType.startsWith("video/") -> FileType.VideoType
        mimeType.startsWith("audio/") -> FileType.AudioType
        mimeType.startsWith("text/") -> FileType.TextType
        else -> FileType.UnknownType
    }
}

fun FileType.toPromptCategory(): PromptCategory {
    return when (this) {
        FileType.ImageType -> PromptCategory.ImageType
        FileType.VideoType -> PromptCategory.VideoType
        FileType.AudioType -> PromptCategory.AudioType
        FileType.PdfType, FileType.WordType, FileType.ExcelType, FileType.PowerPointType, FileType.TextType -> PromptCategory.PdfType
        else -> PromptCategory.UnknownType
    }
}