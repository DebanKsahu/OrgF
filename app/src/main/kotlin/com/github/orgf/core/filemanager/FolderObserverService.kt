package com.github.orgf.core.filemanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.github.orgf.core.ServiceState
import com.github.orgf.core.filemanager.models.NewFileEvent
import com.github.orgf.utils.enums.getFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class FolderObserverService: Service() {

    private var folderObserver: FileObserver? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val serviceState: ServiceState by inject()

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
        serviceScope.cancel()
    }

    private fun startFolderObserverService(uri: Uri) {
        val absoluteFolderPath = toAbsolutePathFromUri(treeUri = uri)

        val folder = File(absoluteFolderPath)
        if (!folder.exists()) {
            return
        }

        folderObserver = @RequiresApi(Build.VERSION_CODES.Q)
        object: FileObserver(
            folder,
            MOVED_TO or CREATE or CLOSE_WRITE
        ) {
            override fun onEvent(event: Int, fileName: String?) {
                if (fileName==null) return
                val fullFilePath = "$absoluteFolderPath/$fileName"
                serviceScope.launch {
                    serviceState.emitNewFileEvent(
                        fileName = fileName,
                        fullFilePath = fullFilePath
                    )
                }
            }
        }

        folderObserver?.startWatching()
    }

    private fun startForegroundNotification() {
        val channelId = "FolderObserverServiceChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Folder Observer Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("OrgF File Organizer Active")
            .setContentText("Listening for file events...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
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