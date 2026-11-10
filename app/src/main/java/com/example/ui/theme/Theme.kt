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
    primary = DokanDarkPrimary,
    onPrimary = DokanDarkOnPrimary,
    primaryContainer = DokanDarkPrimaryContainer,
    secondary = DokanDarkSecondary,
    background = DokanDarkBackground,
    surface = DokanDarkSurface,
    surfaceVariant = DokanDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = DokanGreenPrimary,
    onPrimary = DokanGreenOnPrimary,
    primaryContainer = DokanGreenContainer,
    onPrimaryContainer = DokanGreenOnContainer,
    secondary = DokanGoldSecondary,
    secondaryContainer = DokanGoldContainer,
    onSecondaryContainer = DokanGoldOnContainer,
    tertiary = DokanAmberTertiary,
    tertiaryContainer = DokanAmberContainer,
    onTertiaryContainer = DokanAmberOnContainer,
    background = DokanBackground,
    surface = DokanSurface,
    surfaceVariant = DokanSurfaceVariant,
    outline = DokanOutline
)

@Composable
fun DokanSebaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded emerald retail identity
    content: @Composable () -> Unit,
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
