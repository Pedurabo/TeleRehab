package com.signaldesk.telerehab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = TeleRehabPrimaryDark,
        secondary = TeleRehabSecondaryDark,
        background = TeleRehabBackgroundDark,
        surface = TeleRehabSurfaceDark,
        surfaceVariant = TeleRehabSurfaceVariantDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TeleRehabPrimary,
        secondary = TeleRehabSecondary,
        background = TeleRehabBackground,
        surface = TeleRehabSurface,
        surfaceVariant = TeleRehabSurfaceVariant,
    )

@Composable
fun TeleRehabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme =
            if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            },
        typography = Typography,
        content = content,
    )
}
