package com.github.orgf.core.filemanager.models

import com.github.orgf.utils.enums.FileType

data class NewFileEvent(
    val fullPath: String,
    val fileName: String,
    val fileType: FileType
)
