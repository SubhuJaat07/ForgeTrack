package com.forgetrack.app.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.forgetrack.app.data.local.UserPreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit


data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("body") val body: String = "",
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("published_at") val publishedAt: String = "",
    @SerializedName("assets") val assets: List<GitHubAsset> = emptyList()
)

data class GitHubAsset(
    @SerializedName("name") val name: String = "",
    @SerializedName("browser_download_url") val browserDownloadUrl: String = "",
    @SerializedName("size") val size: Long = 0
)

sealed class UpdateState {
    data object Checking : UpdateState()
    data class UpdateAvailable(
        val release: GitHubRelease,
        val downloadUrl: String
    ) : UpdateState()
    data object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class DownloadComplete(val file: File) : UpdateState()
}

class UpdateService(
    private val context: Context,
    private val userPreferences: UserPreferences? = null
) {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/SubhuJaat07/ForgeTrack/releases/latest"
        private const val TAG_PREFIX = "v"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun checkForUpdate(): UpdateState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateState.Error("GitHub API returned ${response.code}")
            }

            val body = response.body?.string()
                ?: return@withContext UpdateState.Error("Empty response from GitHub")

            val release = gson.fromJson(body, GitHubRelease::class.java)
            if (release.tagName.isBlank()) {
                return@withContext UpdateState.UpToDate
            }

            val latestVersion = parseVersion(release.tagName.removePrefix(TAG_PREFIX))

            // Use the saved "last known version" from DataStore instead of hardcoded BuildConfig
            val savedVersion = try {
                userPreferences?.lastKnownVersion?.first() ?: "1.0.0"
            } catch (_: Exception) {
                "1.0.0"
            }
            val currentVersion = parseVersion(savedVersion)

            if (latestVersion > currentVersion) {
                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk") && !it.name.contains("debug", ignoreCase = true)
                } ?: release.assets.firstOrNull {
                    it.name.endsWith(".apk")
                }

                if (apkAsset != null) {
                    UpdateState.UpdateAvailable(release, apkAsset.browserDownloadUrl)
                } else {
                    UpdateState.UpdateAvailable(release, release.htmlUrl)
                }
            } else {
                UpdateState.UpToDate
            }
        } catch (e: Exception) {
            UpdateState.Error(e.message ?: "Unknown error checking for updates")
        }
    }

    /**
     * Call this after an update is installed or dismissed so the same version
     * is not shown again as "available".
     */
    suspend fun markVersionAsCurrent(versionTag: String) {
        val version = versionTag.removePrefix(TAG_PREFIX)
        userPreferences?.setLastKnownVersion(version)
    }

    suspend fun downloadUpdate(downloadUrl: String): File = withContext(Dispatchers.IO) {
        val fileName = "ForgeTrack-update-${System.currentTimeMillis()}.apk"

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("ForgeTrack Update")
            setDescription("Downloading latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Poll for completion
        var lastProgress = 0
        val query = DownloadManager.Query().setFilterById(downloadId)

        var resultFile: File? = null
        while (resultFile == null) {
            delay(500)
            val cursor: android.database.Cursor? = downloadManager.query(query)
            cursor?.use {
                if (it.moveToFirst()) {
                    val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val status = it.getInt(statusIndex)

                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            val bytesDownloaded = it.getLong(bytesDownloadedIndex)
                            val bytesTotal = it.getLong(bytesTotalIndex)
                            if (bytesTotal > 0) {
                                lastProgress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val uriStr = it.getString(uriIndex)
                            val file = File(Uri.parse(uriStr).path ?: "")
                            if (file.exists()) {
                                resultFile = file
                            } else {
                                throw Exception("Downloaded file not found")
                            }
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIndex = it.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = it.getInt(reasonIndex)
                            throw Exception("Download failed. Reason: $reason")
                        }
                    }
                }
            }
        }
        val file = resultFile!!
        file
    }

    fun installUpdate(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: open downloads folder
            try {
                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun canInstallFromUnknownSources(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0
            ) == 1
        }
    }

    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun parseVersion(versionString: String): Long {
        val parts = versionString.split(".").map { it.toIntOrNull() ?: 0 }
        val major = if (parts.isNotEmpty()) parts[0] else 0
        val minor = if (parts.size > 1) parts[1] else 0
        val patch = if (parts.size > 2) parts[2] else 0
        return major.toLong() * 1000000L + minor.toLong() * 1000L + patch
    }
}
