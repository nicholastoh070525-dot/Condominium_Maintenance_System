package com.example.condominium_maintenance_system.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePale,
    onPrimaryContainer = BlueDeep,
    secondary = InkMuted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF0F8),
    onSecondaryContainer = Ink,
    tertiary = Purple,
    onTertiary = Color.White,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceSoft,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF2F9),
    onSurfaceVariant = InkMuted,
    outline = OutlineSoft,
    error = Coral,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB8FF),
    onPrimary = BlueDeep,
    primaryContainer = Color(0xFF243E75),
    onPrimaryContainer = Color(0xFFE8EEFF),
    secondary = Color(0xFFB8C4D8),
    onSecondary = NightBackground,
    secondaryContainer = Color(0xFF27354B),
    onSecondaryContainer = Color(0xFFE8EEF9),
    tertiary = Color(0xFFC5B9FF),
    onTertiary = Color(0xFF2D2455),
    background = NightBackground,
    onBackground = Color(0xFFF3F6FC),
    surface = NightSurface,
    onSurface = Color(0xFFF3F6FC),
    surfaceVariant = NightSurfaceSoft,
    onSurfaceVariant = Color(0xFFC0CADB),
    outline = NightOutline,
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5C190F)
)

@Composable
fun Condominium_Maintenance_SystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CondoTypography,
        content = content
    )
}
