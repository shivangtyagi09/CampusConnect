package com.example.campusconnect.ui.theme

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
    primary = Sapphire,
    onPrimary = Color.White,
    primaryContainer = SapphireSurface,
    onPrimaryContainer = SapphireDark,
    secondary = RichGold,
    onSecondary = Color.White,
    secondaryContainer = GoldSurface,
    onSecondaryContainer = GoldDark,
    tertiary = Emerald,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSurface,
    onTertiaryContainer = Emerald,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    error = ErrorColor,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorColor,
)

private val DarkColorScheme = darkColorScheme(
    primary = SapphireBright,
    onPrimary = SapphireDark,
    primaryContainer = Sapphire,
    onPrimaryContainer = SapphireBright,
    secondary = GoldBright,
    onSecondary = GoldDark,
    secondaryContainer = GoldDark,
    onSecondaryContainer = GoldBright,
    tertiary = EmeraldBright,
    onTertiary = Emerald,
    tertiaryContainer = Emerald,
    onTertiaryContainer = EmeraldBright,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkOutline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun CampusConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}