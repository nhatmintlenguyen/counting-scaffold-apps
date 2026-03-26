package com.example.dadn_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dynamic color is intentionally disabled so the brand palette is always applied.
private val BrandColorScheme = lightColorScheme(
    primary          = NavyDark,
    onPrimary        = TextWhite,
    secondary        = SafetyOrange,
    onSecondary      = TextWhite,
    background       = BackgroundWhite,
    onBackground     = NavyDark,
    surface          = BackgroundWhite,
    onSurface        = NavyDark,
)

@Composable
fun Dadn_appTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        typography  = Typography,
        content     = content,
    )
}
