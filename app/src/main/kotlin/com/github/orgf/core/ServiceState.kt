package com.github.orgf.core

import com.github.orgf.core.filemanager.models.NewFileEvent
import com.github.orgf.utils.enums.getFileType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer

class ServiceState {

    private val _fileEvent = MutableSharedFlow<NewFileEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val fileEvent = _fileEvent.asSharedFlow()

    suspend fun emitNewFileEvent(fullFilePath: String, fileName: String) {
        _fileEvent.emit(
            NewFileEvent(
                fullPath = fullFilePath,
                fileName = fileName,
                fileType = getFileType(fileName = fileName)
            )
        )
    }

    fun getBufferedFileEventFlow(): Flow<NewFileEvent> {
        return _fileEvent.buffer(
            capacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
}