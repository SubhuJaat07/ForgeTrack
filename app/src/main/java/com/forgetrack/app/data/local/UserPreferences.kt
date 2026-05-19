package com.forgetrack.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_COMPANY = stringPreferencesKey("user_company")
        val USER_ROLE = stringPreferencesKey("user_role")
        val THEME = stringPreferencesKey("theme")
        val CURRENCY = stringPreferencesKey("currency")
        val AUTO_PDF = booleanPreferencesKey("auto_pdf")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val LAST_KNOWN_VERSION = stringPreferencesKey("last_known_version")
    }

    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_ONBOARDED] ?: false }
    val userName: Flow<String> = context.dataStore.data.map { it[Keys.USER_NAME] ?: "" }
    val userCompany: Flow<String> = context.dataStore.data.map { it[Keys.USER_COMPANY] ?: "" }
    val userRole: Flow<String> = context.dataStore.data.map { it[Keys.USER_ROLE] ?: "Contractor" }
    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val currency: Flow<String> = context.dataStore.data.map { it[Keys.CURRENCY] ?: "USD" }
    val autoPdf: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_PDF] ?: true }
    val notifications: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS] ?: true }

    // ── Update tracking ────────────────────────────────────────────────
    val lastKnownVersion: Flow<String> = context.dataStore.data.map { it[Keys.LAST_KNOWN_VERSION] ?: "1.0.0" }

    suspend fun setOnboarded(value: Boolean) { context.dataStore.edit { it[Keys.IS_ONBOARDED] = value } }
    suspend fun setUserName(value: String) { context.dataStore.edit { it[Keys.USER_NAME] = value } }
    suspend fun setUserCompany(value: String) { context.dataStore.edit { it[Keys.USER_COMPANY] = value } }
    suspend fun setUserRole(value: String) { context.dataStore.edit { it[Keys.USER_ROLE] = value } }
    suspend fun setTheme(value: String) { context.dataStore.edit { it[Keys.THEME] = value } }
    suspend fun setCurrency(value: String) { context.dataStore.edit { it[Keys.CURRENCY] = value } }
    suspend fun setAutoPdf(value: Boolean) { context.dataStore.edit { it[Keys.AUTO_PDF] = value } }
    suspend fun setNotifications(value: Boolean) { context.dataStore.edit { it[Keys.NOTIFICATIONS] = value } }
    suspend fun setLastKnownVersion(value: String) { context.dataStore.edit { it[Keys.LAST_KNOWN_VERSION] = value } }
}
