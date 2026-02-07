package com.github.orgf.core.filemanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.github.orgf.core.filemanager.models.NewFileEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class FolderObserverService: Service() {

    private var folderObserver: FileObserver? = null

    companion object {
        private val _newFileEvents = MutableSharedFlow<NewFileEvent>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val newFileEvent = _newFileEvents.asSharedFlow()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriString = intent?.getStringExtra("URI_KEY")

        if (uriString != null) {
            val uri = uriString.toUri()
            startFolderObserverService(uri)
        }

        startForegroundNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        folderObserver?.stopWatching()
    }

    private fun startFolderObserverService(uri: Uri) {
        val absoluteFolderPath = toAbsolutePathFromUri(treeUri = uri)

        Log.d("FileObserverService", "Converted URI to Path: $absoluteFolderPath")

        val folder = File(absoluteFolderPath)
        if (!folder.exists()) {
            Log.e("FileObserverService", "Folder does not exist on disk!")
            return
        }

        folderObserver = @RequiresApi(Build.VERSION_CODES.Q)
        object: FileObserver(
            folder,
            MOVED_TO or CREATE or CLOSE_WRITE
        ) {
            override fun onEvent(event: Int, filePath: String?) {
                if (filePath==null) return
                val fullFilePath = "$absoluteFolderPath/$filePath"
                if (event==MOVED_TO || event==CLOSE_WRITE) {
                    Log.d("FolderObserverService", "🔥 TRUE PUSH EVENT!")
                    Log.d("FolderObserverService", "📂 Folder: $absoluteFolderPath")
                    Log.d("FolderObserverService", "📄 File:   $filePath")
                    Log.d("FolderObserverService", "🚀 FULL:   $fullFilePath")
                }
            }
        }

        folderObserver?.startWatching()
        Log.d("FolderObserverService", "Kernel hook attached. Waiting for files...")
    }

    private fun startForegroundNotification() {
        val channelId = "FolderObserverChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Folder Observer", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("OrgF File Organizer Active")
            .setContentText("Listening for file events...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(1, notification)
    }

    private fun toAbsolutePathFromUri(treeUri: Uri): String {
        val pathSegment = treeUri.lastPathSegment ?: ""
        val pathSegmentParts = pathSegment.split(':')
        val numParts = pathSegmentParts.size

        if (numParts==2 && pathSegmentParts[0]=="primary") {
            return Environment.getExternalStorageDirectory().absolutePath + '/' + pathSegmentParts[1]
        }
        return ""

    }

}