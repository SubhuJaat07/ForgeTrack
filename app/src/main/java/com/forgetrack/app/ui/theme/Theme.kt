package com.forgetrack.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Purple = Color(0xFF6C5CE7)
private val PurpleLight = Color(0xFFa29bfe)
private val PurpleDark = Color(0xFF4d32c7)

private val LightColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEFF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEFF),
    onSecondaryContainer = Color(0xFF21005E),
    tertiary = Color(0xFF006B5E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF89F8E0),
    onTertiaryContainer = Color(0xFF00201B),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF787680),
    outlineVariant = Color(0xFFC8C5D1),
    inverseSurface = Color(0xFF313036),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFC4C1FF),
    surfaceTint = Purple,
    surfaceContainerHighest = Color(0xFFE6E0F0),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC4C1FF),
    onPrimary = Color(0xFF381E73),
    primaryContainer = Color(0xFF4F378D),
    onPrimaryContainer = Color(0xFFE8DEFF),
    secondary = Color(0xFFC4C1FF),
    onSecondary = Color(0xFF381E73),
    secondaryContainer = Color(0xFF4F378D),
    onSecondaryContainer = Color(0xFFE8DEFF),
    tertiary = Color(0xFF6CDBC9),
    onTertiary = Color(0xFF003730),
    tertiaryContainer = Color(0xFF00514A),
    onTertiaryContainer = Color(0xFF89F8E0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0f0f1a),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1a1a2e),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC8C5D1),
    outline = Color(0xFF918F9A),
    outlineVariant = Color(0xFF46464F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313036),
    inversePrimary = Color(0xFF6C5CE7),
    surfaceTint = Color(0xFFC4C1FF),
    surfaceContainerHighest = Color(0xFF36343F),
)

@Composable
fun ForgeTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
