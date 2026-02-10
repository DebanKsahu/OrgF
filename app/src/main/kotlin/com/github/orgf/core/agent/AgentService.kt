package com.github.orgf.core.agent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.orgf.core.ServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AgentService: Service() {

    private val serviceState: ServiceState by inject()
    private val agentServiceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        startAgentService()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        agentServiceScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startAgentService() {
        agentServiceScope.launch {
            serviceState.getBufferedFileEventFlow()
                .flatMapMerge(concurrency = 4) { newFileEvent ->
                    flow {
                        emit(newFileEvent)
                        TODO("Write Logic for handling data events")
                    }
                }.collect { newFileEvent ->
                    TODO("Write the logic for what to do after data get handled")
                }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "AgentServiceChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Agent Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("OrgF File Organizer Active")
            .setContentText("Agent Service is Running...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }
}