package com.github.orgf.utils.enums

import android.webkit.MimeTypeMap
import java.io.File

enum class FileType {
    ImageType,
    VideoType,
    DocumentType,
    AudioType,
    UnknownType
}

fun getFileType(fileName: String): FileType {
    val fileExtension = File(fileName).extension
    val mimeType = MimeTypeMap.getSingleton()?.getMimeTypeFromExtension(fileExtension.lowercase())
        ?: return FileType.UnknownType

    return when {
        mimeType.startsWith("image/") -> FileType.ImageType
        mimeType.startsWith("video/") -> FileType.VideoType
        else -> FileType.UnknownType
    }
}