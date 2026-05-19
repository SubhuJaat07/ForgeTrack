package com.forgetrack.app.ui.screens.update

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.service.UpdateService
import com.forgetrack.app.service.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateService: UpdateService
) : ViewModel() {

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
                    delay(2000)
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

                // Auto-install after brief delay
                delay(1000)
                updateService.installUpdate(file)
            } catch (e: Exception) {
                updateState = UpdateState.Error(e.message ?: "Download failed")
            }
        }
    }

    fun dismissDialog() {
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

@Composable
fun UpdateAvailableDialog(
    viewModel: UpdateViewModel
) {
    val context = LocalContext.current
    val updateState = viewModel.updateState

    if (!viewModel.showDialog) return

    Dialog(
        onDismissRequest = {
            if (updateState !is UpdateState.Downloading && updateState !is UpdateState.Checking) {
                viewModel.dismissDialog()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = updateState is UpdateState.UpToDate || updateState is UpdateState.Error
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Icon
                UpdateAnimatedIcon(updateState = updateState)

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = when (updateState) {
                        is UpdateState.Checking -> "Checking for Updates..."
                        is UpdateState.UpdateAvailable -> "Update Available!"
                        is UpdateState.Downloading -> "Downloading Update..."
                        is UpdateState.DownloadComplete -> "Ready to Install!"
                        is UpdateState.UpToDate -> "You're Up to Date!"
                        is UpdateState.Error -> "Update Error"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = when (updateState) {
                        is UpdateState.Checking -> "Please wait while we check for the latest version..."
                        is UpdateState.UpdateAvailable -> {
                            val body = updateState.release.body
                            if (body.isNotBlank()) {
                                body.lineSequence().firstOrNull()?.take(150) ?: "A new version is available."
                            } else {
                                "Version ${updateState.release.tagName} is ready with improvements and bug fixes."
                            }
                        }
                        is UpdateState.Downloading -> "Downloading the update. This may take a moment depending on your internet speed."
                        is UpdateState.DownloadComplete -> "Download complete! The update will be installed automatically."
                        is UpdateState.UpToDate -> "You already have the latest version of ForgeTrack."
                        is UpdateState.Error -> updateState.message
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Version badge
                if (updateState is UpdateState.UpdateAvailable) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "v${updateState.release.tagName}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress indicator
                if (updateState is UpdateState.Downloading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Buttons
                when (updateState) {
                    is UpdateState.UpdateAvailable -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissDialog() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            ) {
                                Text("Later", modifier = Modifier.padding(vertical = 4.dp))
                            }

                            Button(
                                onClick = {
                                    if (viewModel.canInstallPackages()) {
                                        viewModel.downloadAndInstall(context)
                                    } else {
                                        viewModel.requestInstallPermission()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update Now", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }

                    is UpdateState.Error -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissDialog() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Dismiss")
                            }
                            Button(
                                onClick = { viewModel.checkForUpdateManual() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry")
                            }
                        }
                    }

                    is UpdateState.UpToDate -> {
                        Button(
                            onClick = { viewModel.dismissDialog() },
                            modifier = Modifier.fillMaxWidth(0.6f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("OK", fontWeight = FontWeight.Medium)
                        }
                    }

                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAnimatedIcon(updateState: UpdateState) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconAnim")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val showScale = when (updateState) {
        is UpdateState.UpdateAvailable -> scale
        is UpdateState.Checking -> scale
        else -> 1f
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(showScale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = when (updateState) {
                            is UpdateState.UpdateAvailable -> listOf(Color(0xFF6C5CE7), Color(0xFFa29bfe))
                            is UpdateState.Downloading -> listOf(Color(0xFF0984e3), Color(0xFF74b9ff))
                            is UpdateState.DownloadComplete -> listOf(Color(0xFF00b894), Color(0xFF55efc4))
                            is UpdateState.UpToDate -> listOf(Color(0xFF00b894), Color(0xFF55efc4))
                            is UpdateState.Error -> listOf(Color(0xFFd63031), Color(0xFFff7675))
                            is UpdateState.Checking -> listOf(Color(0xFF6C5CE7), Color(0xFFa29bfe))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            when (updateState) {
                is UpdateState.Checking -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp,
                        color = Color.White
                    )
                }
                is UpdateState.UpdateAvailable -> {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
                is UpdateState.Downloading -> {
                    Icon(Icons.Default.Downloading, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.White)
                }
                is UpdateState.DownloadComplete -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
                is UpdateState.UpToDate -> {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
                is UpdateState.Error -> {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
            }
        }
    }
}
