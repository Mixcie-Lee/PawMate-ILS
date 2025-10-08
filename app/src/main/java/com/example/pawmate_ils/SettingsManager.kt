package com.example.pawmate_ils

import android.content.Context

class SettingsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun isPrivacyEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY, false)
    fun setPrivacyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY, enabled).apply()
    }

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "User") ?: "User"
    fun setUsername(name: String) {
        prefs.edit().putString(KEY_USERNAME, name).apply()
    }

    fun getProfilePhotoUri(): String? = prefs.getString(KEY_PFP_URI, null)
    fun setProfilePhotoUri(uri: String?) {
        prefs.edit().putString(KEY_PFP_URI, uri).apply()
    }

    companion object {
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_PRIVACY = "privacy_enabled"
        private const val KEY_USERNAME = "user_name"
        private const val KEY_PFP_URI = "pfp_uri"
    }
}


