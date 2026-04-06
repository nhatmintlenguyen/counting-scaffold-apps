package com.example.dadn_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.ui.screens.AppNavGraph
import com.example.dadn_app.ui.screens.AppRoutes
import com.example.dadn_app.ui.theme.Dadn_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize EncryptedSharedPreferences once, before any token reads/writes.
        TokenManager.init(applicationContext)

        // Auto-login: if a token is already stored the user skips the Login screen.
        val startDestination = if (TokenManager.isLoggedIn)
            AppRoutes.MAIN
        else
            AppRoutes.LOGIN

        enableEdgeToEdge()
        setContent {
            Dadn_appTheme {
                AppNavGraph(startDestination = startDestination)
            }
        }
    }
}
