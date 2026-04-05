package com.example.dadn_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dynamic color is intentionally disabled so the brand palette is always applied.
private val BrandColorScheme = lightColorScheme(
    primary                 = Primary,
    onPrimary               = OnPrimary,
    primaryContainer        = PrimaryContainer,
    onPrimaryContainer      = OnPrimaryContainer,
    inversePrimary          = InversePrimary,
    secondary               = Secondary,
    onSecondary             = OnSecondary,
    secondaryContainer      = SecondaryContainer,
    onSecondaryContainer    = OnSecondaryContainer,
    tertiary                = Tertiary,
    onTertiary              = OnTertiary,
    tertiaryContainer       = TertiaryContainer,
    onTertiaryContainer     = OnTertiaryContainer,
    background              = Background,
    onBackground            = OnBackground,
    surface                 = Surface,
    onSurface               = OnSurface,
    surfaceVariant          = SurfaceVariant,
    onSurfaceVariant        = OnSurfaceVariant,
    inverseSurface          = InverseSurface,
    inverseOnSurface        = InverseOnSurface,
    error                   = Error,
    onError                 = OnError,
    errorContainer          = ErrorContainer,
    onErrorContainer        = OnErrorContainer,
    outline                 = Outline,
    outlineVariant          = OutlineVariant,
    surfaceTint             = SurfaceTint,
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
