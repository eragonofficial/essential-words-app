package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicGlassColorScheme = darkColorScheme(
    primary = GlassCyanAccent,
    onPrimary = CosmicDeepNavy,
    primaryContainer = GlassCyanFill,
    onPrimaryContainer = TextPrimaryWhite,
    secondary = GlassVioletAccent,
    onSecondary = CosmicDeepNavy,
    secondaryContainer = GlassVioletFill,
    onSecondaryContainer = TextPrimaryWhite,
    tertiary = GlassEmeraldAccent,
    onTertiary = CosmicDeepNavy,
    tertiaryContainer = GlassEmeraldFill,
    onTertiaryContainer = TextPrimaryWhite,
    background = Color.Transparent,
    onBackground = TextPrimaryWhite,
    surface = GlassTier2Fill,
    onSurface = TextPrimaryWhite,
    surfaceVariant = GlassTier1Fill,
    onSurfaceVariant = TextBodyTranslucent,
    surfaceContainerLowest = Color.Transparent,
    surfaceContainerLow = GlassDrawerTint,
    surfaceContainer = GlassTier2Fill,
    surfaceContainerHigh = GlassTier2ElevatedFill,
    surfaceContainerHighest = GlassTier3Fill,
    outline = TextMetaTranslucent,
    outlineVariant = GlassTier1Border,
    error = GlassRoseAccent,
    onError = TextPrimaryWhite,
    errorContainer = GlassRoseFill,
    onErrorContainer = TextPrimaryWhite,
    scrim = GlassScrimDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicGlassColorScheme,
        typography = Typography,
        content = content
    )
}
