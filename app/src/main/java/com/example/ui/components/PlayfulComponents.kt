package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TreasureRarity
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark
import com.example.ui.theme.SunnyGoldLight

@Composable
fun SkyCloudsBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sky_clouds")
    val drift by transition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )
    val twinkle by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBEE9FF),
                        SkyBlueSurface,
                        Color(0xFFF7FCFF)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Fluffy rounded cartoon clouds
            drawCircle(Color.White.copy(alpha = 0.72f), radius = w * 0.16f, center = Offset(w * 0.16f + drift, h * 0.11f))
            drawCircle(Color.White.copy(alpha = 0.72f), radius = w * 0.12f, center = Offset(w * 0.28f + drift, h * 0.12f))
            drawCircle(Color.White.copy(alpha = 0.65f), radius = w * 0.18f, center = Offset(w * 0.84f - drift, h * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.65f), radius = w * 0.13f, center = Offset(w * 0.72f - drift, h * 0.19f))
            // Twinkling stars
            drawCircle(SunnyGold.copy(alpha = twinkle * 0.75f), radius = 8f, center = Offset(w * 0.48f, h * 0.08f))
            drawCircle(SunnyGold.copy(alpha = (1.3f - twinkle).coerceIn(0.2f, 0.8f)), radius = 6f, center = Offset(w * 0.90f, h * 0.09f))
            drawCircle(SkyBlueLight.copy(alpha = twinkle * 0.6f), radius = 7f, center = Offset(w * 0.10f, h * 0.26f))
        }
        content()
    }
}

@Composable
fun ChunkyActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    containerColor: Color = SunnyGold,
    shadowColor: Color = SunnyGoldDark,
    contentColor: Color = DeepOceanNavy,
    enabled: Boolean = true,
    testTagName: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "btn_scale"
    )

    val actualColor = if (enabled) containerColor else Color(0xFFD5E2EB)
    val actualShadow = if (enabled) shadowColor else Color(0xFFB4C6D4)
    val actualContent = if (enabled) contentColor else Color(0xFF6B7E8C)

    Box(
        modifier = modifier
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier)
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(actualShadow)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(bottom = if (pressed && enabled) 2.dp else 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(actualColor)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!emoji.isNullOrBlank()) {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = actualContent,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RarityBadge(rarity: TreasureRarity, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = rarity.bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = rarity.labelKo,
            style = MaterialTheme.typography.labelMedium,
            color = rarity.badgeColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CoinPill(coins: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SunnyGoldLight,
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🪙", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = coins.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = DeepOceanNavy
            )
        }
    }
}

@Composable
fun LevelExpBar(
    level: Long,
    currentExp: Long,
    needExp: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (needExp > 0L) (currentExp.toFloat() / needExp.toFloat()).coerceIn(0f, 1f) else 1f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = SkyBluePrimary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Lv.$level 탐험가",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = "$currentExp / $needExp EXP",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepOceanNavy
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
                .background(Color(0xFFD7EEFC))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(SkyBluePrimary, Color(0xFF2ECC71))
                        )
                    )
            )
        }
    }
}
