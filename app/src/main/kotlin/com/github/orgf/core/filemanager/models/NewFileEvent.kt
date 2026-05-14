package com.github.orgf.core.filemanager.models

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.github.orgf.utils.enums.FileType

data class NewFileEvent(
    val fullPath: String,
    val fileName: String,
    val fileType: FileType,
    val rootDoc: DocumentFile,
    val rootFolderUri: Uri
)
