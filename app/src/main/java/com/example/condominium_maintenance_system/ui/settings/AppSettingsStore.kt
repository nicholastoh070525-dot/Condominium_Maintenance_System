package com.example.condominium_maintenance_system.ui.settings

import android.content.Context

class AppSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "condotrack_settings",
        Context.MODE_PRIVATE
    )

    val notificationsEnabled: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    val darkMode: Boolean?
        get() = if (preferences.contains(KEY_DARK_MODE)) {
            preferences.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    private companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_DARK_MODE = "dark_mode"
    }
}
