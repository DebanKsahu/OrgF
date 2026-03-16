package com.github.orgf.folderpickerscreen.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.github.orgf.core.agent.AgentService
import com.github.orgf.core.filemanager.FolderObserverService
import com.github.orgf.utils.APP_PREFERENCES_KEY
import com.github.orgf.utils.SELECTED_FOLDER_URI_KEY

class FolderPickerViewModel : ViewModel() {


    fun saveWorkspaceUri(appContext: Context, workspaceUri: Uri) {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        appContext.contentResolver.takePersistableUriPermission(workspaceUri, takeFlags)

        val sharedPrefs = appContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE)
        sharedPrefs.edit { putString(SELECTED_FOLDER_URI_KEY, workspaceUri.toString()) }
    }

    fun startRequiredBackgroundService(appContext: Context, workspaceUri: Uri) {
        startFolderObserverService(appContext = appContext, workspaceUri = workspaceUri)
        startAgentService(appContext = appContext)
    }

    private fun startFolderObserverService(appContext: Context, workspaceUri: Uri) {
        val intent = Intent(appContext, FolderObserverService::class.java).apply {
            putExtra(SELECTED_FOLDER_URI_KEY, workspaceUri.toString())
        }

        if (Build.VERSION.SDK_INT >= 26) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

    private fun startAgentService(appContext: Context) {
        val intent = Intent(appContext, AgentService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

}