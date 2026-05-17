package com.example.nhathuoc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = GreenPrimary,
    onPrimary            = GreenOnPrimary,
    primaryContainer     = GreenPrimaryContainer,
    onPrimaryContainer   = GreenOnPrimaryContainer,
    secondary            = GreenSecondary,
    onSecondary          = GreenOnSecondary,
    secondaryContainer   = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary             = GoldTertiary,
    onTertiary           = GoldOnTertiary,
    tertiaryContainer    = GoldTertiaryContainer,
    onTertiaryContainer  = GoldOnTertiaryContainer,
    error                = ErrorColor,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,
    background           = Background,
    onBackground         = OnBackground,
    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,
    outline              = OutlineColor,
    outlineVariant       = OutlineVariant,
    inverseSurface       = InverseSurface,
    inverseOnSurface     = InverseOnSurface,
    inversePrimary       = InversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary              = GreenPrimaryDark,
    onPrimary            = GreenOnPrimaryDark,
    primaryContainer     = GreenPrimaryContainerDark,
    onPrimaryContainer   = GreenOnPrimaryContainerDark,
    secondary            = GreenSecondaryDark,
    onSecondary          = GreenOnSecondaryDark,
    secondaryContainer   = GreenSecondaryContainerDark,
    onSecondaryContainer = GreenOnSecondaryContainerDark,
    tertiary             = GoldTertiaryDark,
    onTertiary           = GoldOnTertiaryDark,
    tertiaryContainer    = GoldTertiaryContainerDark,
    onTertiaryContainer  = GoldOnTertiaryContainerDark,
    error                = ErrorDark,
    onError              = OnErrorDark,
    errorContainer       = ErrorContainerDark,
    onErrorContainer     = OnErrorContainerDark,
    background           = BackgroundDark,
    onBackground         = OnBackgroundDark,
    surface              = SurfaceDark,
    onSurface            = OnSurfaceDark,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = OnSurfaceVariantDark,
    outline              = OutlineColorDark,
    outlineVariant       = OutlineVariantDark,
)

@Composable
fun NhathuocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge transparent status bar
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
