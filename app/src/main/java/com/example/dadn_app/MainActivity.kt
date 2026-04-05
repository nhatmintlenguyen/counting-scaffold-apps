package com.example.dadn_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dadn_app.ui.screens.MainScreen
import com.example.dadn_app.ui.theme.Dadn_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Dadn_appTheme {
                MainScreen()
            }
        }
    }
}
