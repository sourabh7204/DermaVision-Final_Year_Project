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
    primary = CleanTealPrimaryDark,
    onPrimary = CleanTealOnPrimaryDark,
    primaryContainer = CleanTealContainerDark,
    onPrimaryContainer = CleanTealOnContainerDark,
    secondary = CleanSlateSecondaryDark,
    onSecondary = CleanSlateOnSecondaryDark,
    secondaryContainer = CleanSlateContainerDark,
    onSecondaryContainer = CleanSlateOnContainerDark,
    tertiary = CleanTealTertiaryDark,
    onTertiary = CleanTealOnTertiaryDark,
    tertiaryContainer = CleanTealTertiaryContainerDark,
    onTertiaryContainer = CleanTealOnTertiaryContainerDark,
    background = CleanBackgroundDark,
    onBackground = CleanOnBackgroundDark,
    surface = CleanSurfaceDark,
    onSurface = CleanOnSurfaceDark,
    surfaceVariant = CleanSurfaceVariantDark,
    onSurfaceVariant = CleanOnSurfaceVariantDark,
    outline = CleanOutlineDark,
    outlineVariant = CleanOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = CleanTealPrimaryLight,
    onPrimary = CleanTealOnPrimaryLight,
    primaryContainer = CleanTealContainerLight,
    onPrimaryContainer = CleanTealOnContainerLight,
    secondary = CleanSlateSecondaryLight,
    onSecondary = CleanSlateOnSecondaryLight,
    secondaryContainer = CleanSlateContainerLight,
    onSecondaryContainer = CleanSlateOnContainerLight,
    tertiary = CleanTealTertiaryLight,
    onTertiary = CleanTealOnTertiaryLight,
    tertiaryContainer = CleanTealTertiaryContainerLight,
    onTertiaryContainer = CleanTealOnTertiaryContainerLight,
    background = CleanBackgroundLight,
    onBackground = CleanOnBackgroundLight,
    surface = CleanSurfaceLight,
    onSurface = CleanOnSurfaceLight,
    surfaceVariant = CleanSurfaceVariantLight,
    onSurfaceVariant = CleanOnSurfaceVariantLight,
    outline = CleanOutlineLight,
    outlineVariant = CleanOutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
