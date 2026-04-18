package com.example.dadn_app.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
    private const val KEY_EMAIL   = "user_email"
    private const val KEY_NAME    = "user_name"
    private const val KEY_AVATAR  = "user_avatar"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        prefs = EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Save tokens, email, and name after a successful login or register. */
    fun saveTokens(accessToken: String, refreshToken: String, email: String? = null, name: String? = null) {
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
            if (email != null) putString(KEY_EMAIL, email)
            if (name != null) putString(KEY_NAME, name)
        }
    }

    /** Returns null if no token has been stored yet (user not logged in). */
    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)

    val userEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)

    val userName: String?
        get() = prefs.getString(KEY_NAME, null)

    val userAvatar: String?
        get() = prefs.getString(KEY_AVATAR, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    /** True when a user is currently logged in. */
    val isLoggedIn: Boolean
        get() = accessToken != null

    /** Call on logout to erase only security credentials, keeping profile info. */
    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
            // Keep KEY_EMAIL, KEY_NAME, KEY_AVATAR for a personalized experience
        }
    }

    /** Save only the avatar URI string. */
    fun saveAvatar(uri: String) {
        prefs.edit { putString(KEY_AVATAR, uri) }
    }
}
