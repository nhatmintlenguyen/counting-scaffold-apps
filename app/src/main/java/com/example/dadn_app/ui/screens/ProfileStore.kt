package com.example.dadn_app.ui.screens

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ProfileStore {
    const val PREFS = "scp_profile_prefs"
    const val KEY_AVATAR_URI = "avatar_uri"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"

    fun saveAvatarToInternal(context: Context, uri: Uri): Uri? {
        return try {
            val file = File(context.filesDir, "user_avatar_permanent.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            } ?: return null
            Uri.fromFile(file)
        } catch (_: Exception) {
            null
        }
    }
}
