package com.forgetrack.app.ui.screens.settings

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.local.UserPreferences
import com.forgetrack.app.data.repository.ClientRepository
import com.forgetrack.app.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val jobRepository: JobRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    /**
     * User profile information derived from DataStore preferences.
     */
    val userProfile: StateFlow<UserProfile> = combine(
        userPreferences.userName,
        userPreferences.userRole
    ) { name, role ->
        UserProfile(
            name = name.ifBlank { "John Doe" },
            role = role.ifBlank { "Field Service Pro" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    /**
     * Current theme preference, mapped from the stored string to [AppTheme] enum.
     */
    val selectedTheme: StateFlow<AppTheme> = userPreferences.theme
        .map { stored ->
            AppTheme.entries.firstOrNull { it.name.equals(stored, ignoreCase = true) }
                ?: AppTheme.SYSTEM
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    /**
     * Whether push notifications are enabled.
     */
    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Whether PDF reports are auto-generated on job completion.
     */
    val autoPdfEnabled: StateFlow<Boolean> = userPreferences.autoPdf
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Currently selected display currency, mapped from stored string to [Currency] enum.
     */
    val selectedCurrency: StateFlow<Currency> = userPreferences.currency
        .map { stored ->
            Currency.entries.firstOrNull { it.code.equals(stored, ignoreCase = true) }
                ?: Currency.USD
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Currency.USD
        )

    // ─── Theme ───────────────────────────────────────────────────────────

    /**
     * Persists the selected app theme to DataStore.
     */
    fun onThemeChanged(theme: AppTheme) {
        viewModelScope.launch { userPreferences.setTheme(theme.name.lowercase()) }
    }

    // ─── Notifications ──────────────────────────────────────────────────

    /**
     * Enables or disables push notifications.
     */
    fun onNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setNotifications(enabled) }
    }

    // ─── Auto PDF ───────────────────────────────────────────────────────

    /**
     * Enables or disables automatic PDF generation on job completion.
     */
    fun onAutoPdfChanged(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAutoPdf(enabled) }
    }

    // ─── Currency ───────────────────────────────────────────────────────

    /**
     * Persists the selected display currency to DataStore.
     */
    fun onCurrencyChanged(currency: Currency) {
        viewModelScope.launch { userPreferences.setCurrency(currency.code) }
    }

    // ─── Data Management ────────────────────────────────────────────────

    /**
     * Exports all jobs and clients as a JSON backup file saved to the
     * device's Downloads directory via MediaStore.
     */
    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val allJobs = jobRepository.getAllJobs().first()
                val allClients = clientRepository.getAllClients().first()

                val json = buildString {
                    append("{\n")
                    append("  \"exportedAt\": ${System.currentTimeMillis()},\n")
                    append("  \"jobs\": [\n")
                    allJobs.forEachIndexed { index, job ->
                        append("    ${buildJobJson(job)}")
                        if (index < allJobs.lastIndex) append(",")
                        append("\n")
                    }
                    append("  ],\n")
                    append("  \"clients\": [\n")
                    allClients.forEachIndexed { index, client ->
                        append("    ${buildClientJson(client)}")
                        if (index < allClients.lastIndex) append(",")
                        append("\n")
                    }
                    append("  ]\n")
                    append("}")
                }

                val fileName = "forgetrack_backup_${System.currentTimeMillis()}.json"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
            } catch (_: Exception) {
                // Silently fail — in a production app this would surface
                // an error via a UI state or toast.
            }
        }
    }

    /**
     * Permanently deletes all jobs, clients, and resets preferences
     * to their default values.
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                val allJobs = jobRepository.getAllJobs().first()
                allJobs.forEach { jobRepository.deleteJob(it.id) }

                val allClients = clientRepository.getAllClients().first()
                allClients.forEach { clientRepository.deleteClient(it.id) }

                userPreferences.setOnboarded(false)
                userPreferences.setTheme("system")
                userPreferences.setCurrency("USD")
                userPreferences.setAutoPdf(true)
                userPreferences.setNotifications(true)
                userPreferences.setUserName("")
                userPreferences.setCompany("")
                userPreferences.setUserRole("Contractor")
            } catch (_: Exception) {
                // Silently fail — in a production app this would surface
                // an error via a UI state or toast.
            }
        }
    }

    /**
     * Resets the onboarding flag so the onboarding screen is shown again
     * on the next app launch.
     */
    fun resetOnboarding() {
        viewModelScope.launch { userPreferences.setOnboarded(false) }
    }

    // ─── JSON helpers ───────────────────────────────────────────────────

    private fun buildJobJson(job: com.forgetrack.app.data.model.Job): String {
        return """
            {
                "id": "${escapeJson(job.id)}",
                "title": "${escapeJson(job.title)}",
                "description": "${escapeJson(job.description)}",
                "clientId": "${escapeJson(job.clientId)}",
                "clientName": "${escapeJson(job.clientName)}",
                "status": "${job.status}",
                "priority": "${job.priority}",
                "scheduledDate": ${job.scheduledDate},
                "address": "${escapeJson(job.address)}",
                "revenue": ${job.revenue},
                "cost": ${job.cost},
                "totalDuration": ${job.totalDuration},
                "notes": "${escapeJson(job.notes)}",
                "createdAt": ${job.createdAt},
                "updatedAt": ${job.updatedAt}
            }
        """.trimIndent().replace("\n", " ")
    }

    private fun buildClientJson(client: com.forgetrack.app.data.model.Client): String {
        return """
            {
                "id": "${escapeJson(client.id)}",
                "name": "${escapeJson(client.name)}",
                "company": "${escapeJson(client.company)}",
                "email": "${escapeJson(client.email)}",
                "phone": "${escapeJson(client.phone)}",
                "address": "${escapeJson(client.address)}",
                "notes": "${escapeJson(client.notes)}",
                "createdAt": ${client.createdAt},
                "updatedAt": ${client.updatedAt}
            }
        """.trimIndent().replace("\n", " ")
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
