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
import androidx.documentfile.provider.DocumentFile
import com.github.orgf.core.ServiceState
import com.github.orgf.utils.SELECTED_FOLDER_URI_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class FolderObserverService: Service() {

    private var folderObserver: FileObserver? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val serviceState: ServiceState by inject()

    private val processedFiles = mutableSetOf<String>()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        Log.d("FolderObserverService", "Service onCreate: Foreground notification started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FolderObserverService", "onStartCommand triggered!")
        val uriString = intent?.getStringExtra(SELECTED_FOLDER_URI_KEY)

        if (uriString != null) {
            val uri = uriString.toUri()
            startFolderObserverService(uri)
        }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        folderObserver?.stopWatching()
        serviceScope.cancel()
    }

    private fun startFolderObserverService(uri: Uri) {
        Log.d("FolderObserverService", "Starting for URI: $uri")
        val absoluteFolderPath = toAbsolutePathFromUri(treeUri = uri)
        Log.d("FolderObserverService", "Resolved Path: $absoluteFolderPath")

        val folder = File(absoluteFolderPath)
        if (!folder.exists()) {
            Log.e(
                "FolderObserverService",
                "PATH DOES NOT EXIST or is inaccessible: $absoluteFolderPath"
            )
            return
        }

        Log.d("FolderObserverService", "Folder exists. Initializing FileObserver...")

        folderObserver = @RequiresApi(Build.VERSION_CODES.Q)
        object: FileObserver(
            folder,
            MOVED_TO or CREATE or CLOSE_WRITE
        ) {
            override fun onEvent(event: Int, fileName: String?) {
                Log.d("FolderObserverService", "Event: $event, FileName: $fileName")
                if (fileName==null) return

                val isMoved = (event and MOVED_TO)!=0
                val isClose = (event and CLOSE_WRITE)!=0
                if (!isMoved and !isClose) return

                synchronized(processedFiles) {
                    if (processedFiles.contains(fileName)) return
                    processedFiles.add(fileName)
                }

                val fullFilePath = "$absoluteFolderPath/$fileName"
                Log.d("FolderObserverService", "File Event Detected: $fullFilePath")
                serviceScope.launch {
                    val rootFolderUri = uri
                    val rootDoc =
                        DocumentFile.fromTreeUri(applicationContext, rootFolderUri) ?: return@launch

                    serviceState.emitNewFileEvent(
                        fileName = fileName,
                        fullFilePath = fullFilePath,
                        rootDoc = rootDoc,
                        rootFolderUri = rootFolderUri
                    )

                    delay(2000)
                    synchronized(processedFiles) {
                        processedFiles.remove(fileName)
                    }
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