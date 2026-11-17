package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TreasureColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyBlueSurface,
    onPrimaryContainer = DeepOceanNavy,
    secondary = SunnyGold,
    onSecondary = DeepOceanNavy,
    secondaryContainer = SunnyGoldLight,
    onSecondaryContainer = DeepOceanNavy,
    tertiary = MintEmerald,
    onTertiary = Color.White,
    tertiaryContainer = MintEmeraldLight,
    onTertiaryContainer = DeepOceanNavy,
    error = CoralBerry,
    onError = Color.White,
    errorContainer = CoralBerryLight,
    onErrorContainer = CoralBerryDark,
    background = SkyBlueSurface,
    onBackground = DeepOceanNavy,
    surface = SkyCloudWhite,
    onSurface = DeepOceanNavy,
    surfaceVariant = Color(0xFFDFF1FD),
    onSurfaceVariant = SoftSlateText,
    outline = Color(0xFFB3D9F2)
)

val TreasureShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TreasureRadarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TreasureColorScheme,
        typography = Typography,
        shapes = TreasureShapes,
        content = content
    )
}
