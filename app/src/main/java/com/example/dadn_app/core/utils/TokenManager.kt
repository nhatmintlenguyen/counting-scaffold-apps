package com.example.dadn_app.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Singleton helper that stores and retrieves JWT tokens using
 * EncryptedSharedPreferences (androidx.security:security-crypto:1.0.0).
 *
 * The underlying data is AES-256-GCM encrypted at the OS level via the
 * Android Keystore, so tokens are never written to disk in plain text.
 *
 * Usage:
 *   TokenManager.init(applicationContext)   ← call once in MainActivity.onCreate()
 *   TokenManager.saveTokens(access, refresh)
 *   TokenManager.accessToken
 *   TokenManager.clearTokens()
 */
object TokenManager {

    private const val PREFS_FILE  = "scp_secure_prefs"
    private const val KEY_ACCESS  = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_EMAIL = "user_email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        // MasterKeys.getOrCreate() generates (or retrieves) the AES-256-GCM key
        // stored inside the Android Keystore — it never leaves the secure hardware.
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        prefs = EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Save the access token after a successful login. */
    fun saveAccessToken(accessToken: String, email: String? = null) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .remove(KEY_REFRESH)
            .apply {
                if (email == null) remove(KEY_USER_EMAIL) else putString(KEY_USER_EMAIL, email)
            }
            .apply()
    }

    /** Kept for compatibility if a future backend adds refresh tokens again. */
    fun saveTokens(accessToken: String, refreshToken: String? = null, email: String? = null) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .apply {
                if (refreshToken == null) remove(KEY_REFRESH) else putString(KEY_REFRESH, refreshToken)
                if (email == null) remove(KEY_USER_EMAIL) else putString(KEY_USER_EMAIL, email)
            }
            .apply()
    }

    /** Returns null if no token has been stored yet (user not logged in). */
    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    val userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)

    /** True when a user is currently logged in. */
    val isLoggedIn: Boolean
        get() = accessToken != null

    /** Call on logout to erase all stored credentials. */
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
