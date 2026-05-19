package com.forgetrack.app.ui.screens.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.local.UserPreferences
import com.forgetrack.app.service.UpdateService
import com.forgetrack.app.service.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val application: Application,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val updateService = UpdateService(application, userPreferences)

    var updateState by mutableStateOf<UpdateState>(UpdateState.UpToDate)
        private set

    var showDialog by mutableStateOf(false)
        private set

    private var latestRelease: com.forgetrack.app.service.GitHubRelease? = null

    /**
     * Called on app startup to check for updates in the background.
     * Only shows dialog if an update is actually available.
     */
    fun checkForUpdateSilent() {
        viewModelScope.launch {
            try {
                updateState = UpdateState.Checking
                val result = updateService.checkForUpdate()
                updateState = result

                if (result is UpdateState.UpdateAvailable) {
                    latestRelease = result.release
                    // Delay showing dialog slightly so user sees the app first
                    delay(3000)
                    showDialog = true
                }
            } catch (_: Exception) {
                // Silently fail - don't interrupt the user
                updateState = UpdateState.UpToDate
            }
        }
    }

    /**
     * Manual check initiated by user from Settings.
     */
    fun checkForUpdateManual() {
        viewModelScope.launch {
            updateState = UpdateState.Checking
            showDialog = true

            try {
                val result = updateService.checkForUpdate()
                updateState = result
            } catch (e: Exception) {
                updateState = UpdateState.Error(e.message ?: "Failed to check")
            }
        }
    }

    /**
     * Downloads and installs the update APK.
     */
    fun downloadAndInstall(context: android.content.Context) {
        val state = updateState
        if (state !is UpdateState.UpdateAvailable) return

        viewModelScope.launch {
            try {
                updateState = UpdateState.Downloading(0)
                val file = updateService.downloadUpdate(state.downloadUrl)
                updateState = UpdateState.DownloadComplete(file)

                // Mark this version as current so it won't show up again
                latestRelease?.let { updateService.markVersionAsCurrent(it.tagName) }

                // Auto-install after brief delay
                delay(1000)
                updateService.installUpdate(file)
            } catch (e: Exception) {
                updateState = UpdateState.Error(e.message ?: "Download failed")
            }
        }
    }

    /**
     * User dismissed the update dialog - mark version so it doesn't keep popping up.
     */
    fun dismissDialog() {
        latestRelease?.let {
            viewModelScope.launch {
                updateService.markVersionAsCurrent(it.tagName)
            }
        }
        showDialog = false
    }

    fun requestInstallPermission() {
        updateService.requestInstallPermission()
    }

    fun canInstallPackages(): Boolean = updateService.canInstallFromUnknownSources()

    fun openReleasePage(context: android.content.Context) {
        latestRelease?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.htmlUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
