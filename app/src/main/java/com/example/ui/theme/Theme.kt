package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Slate900,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    secondary = SkyBlue,
    onSecondary = PureWhite,
    secondaryContainer = Slate800,
    onSecondaryContainer = SkyLight,
    tertiary = AmberWarning,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate500
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = PureWhite,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = SkyBlue,
    onSecondary = PureWhite,
    secondaryContainer = SkyLight,
    onSecondaryContainer = Slate900,
    tertiary = AmberWarning,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted fresh laundry palette by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
