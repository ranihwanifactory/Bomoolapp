package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoxelCharacterAvatar(
    avatarKey: String,
    modifier: Modifier = Modifier,
    fallbackFontSize: Dp = 28.dp
) {
    val isVoxel = avatarKey.startsWith("vx_")
    if (!isVoxel) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = avatarKey.ifBlank { "🧱" }, fontSize = fallbackFontSize.value.sp)
        }
        return
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val cx = w / 2f
        val cy = h / 2f + s * 0.06f

        fun px(v: Float) = s * v

        val headSize = px(0.56f)
        val headLeft = cx - headSize / 2f
        val headTop = cy - headSize / 2f
        val corner = CornerRadius(px(0.08f), px(0.08f))

        when (avatarKey) {
            "vx_explorer" -> {
                // Warm blocky adventurer with explorer cap & gold pin
                drawRoundRect(
                    color = Color(0xFF39A0ED),
                    topLeft = Offset(cx - px(0.24f), headTop + headSize - px(0.02f)),
                    size = Size(px(0.48f), px(0.18f)),
                    cornerRadius = CornerRadius(px(0.06f), px(0.06f))
                )
                drawRoundRect(
                    color = Color(0xFFFFD8B0),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headSize, headSize),
                    cornerRadius = corner
                )
                // 3D side shade
                drawRoundRect(
                    color = Color(0xFFF2BE8E),
                    topLeft = Offset(headLeft + headSize * 0.78f, headTop),
                    size = Size(headSize * 0.22f, headSize),
                    cornerRadius = corner
                )
                // Explorer Cap
                drawRoundRect(
                    color = Color(0xFFD9822B),
                    topLeft = Offset(headLeft - px(0.03f), headTop - px(0.10f)),
                    size = Size(headSize + px(0.06f), px(0.20f)),
                    cornerRadius = CornerRadius(px(0.06f), px(0.06f))
                )
                drawRoundRect(
                    color = Color(0xFFA85C16),
                    topLeft = Offset(headLeft + px(0.04f), headTop + px(0.06f)),
                    size = Size(headSize + px(0.10f), px(0.07f)),
                    cornerRadius = CornerRadius(px(0.04f), px(0.04f))
                )
                // Gold emblem on cap
                drawCircle(Color(0xFFFFD54F), radius = px(0.045f), center = Offset(cx, headTop))
                // Pixel eyes & blush & smile
                drawPixelEyesAndSmile(cx, headTop + headSize * 0.56f, px(0.055f), Color(0xFF1F2D3D))
            }

            "vx_obby" -> {
                // Neon headset obby jumper with cool shades
                drawRoundRect(
                    color = Color(0xFF00C2FF),
                    topLeft = Offset(cx - px(0.24f), headTop + headSize - px(0.02f)),
                    size = Size(px(0.48f), px(0.18f)),
                    cornerRadius = CornerRadius(px(0.06f), px(0.06f))
                )
                // Headset band
                drawRoundRect(
                    color = Color(0xFFFF4D6D),
                    topLeft = Offset(headLeft - px(0.06f), headTop - px(0.06f)),
                    size = Size(headSize + px(0.12f), headSize * 0.55f),
                    cornerRadius = CornerRadius(px(0.10f), px(0.10f))
                )
                // Head
                drawRoundRect(
                    color = Color(0xFFFFE066),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headSize, headSize),
                    cornerRadius = corner
                )
                // Hair tuft
                drawRoundRect(
                    color = Color(0xFF3E2723),
                    topLeft = Offset(headLeft, headTop - px(0.02f)),
                    size = Size(headSize, px(0.14f)),
                    cornerRadius = CornerRadius(px(0.05f), px(0.05f))
                )
                // Cool Pixel Visor / Shades
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(headLeft + px(0.06f), headTop + headSize * 0.38f),
                    size = Size(headSize * 0.36f, px(0.13f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(headLeft + headSize * 0.54f, headTop + headSize * 0.38f),
                    size = Size(headSize * 0.36f, px(0.13f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                drawRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(headLeft + px(0.09f), headTop + headSize * 0.41f),
                    size = Size(px(0.08f), px(0.04f))
                )
                // Big grin
                drawRoundRect(
                    color = Color(0xFF1F2D3D),
                    topLeft = Offset(cx - px(0.09f), headTop + headSize * 0.72f),
                    size = Size(px(0.18f), px(0.06f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
            }

            "vx_knight" -> {
                // Diamond Cube Knight
                drawRoundRect(
                    color = Color(0xFF00B4D8),
                    topLeft = Offset(headLeft - px(0.04f), headTop - px(0.06f)),
                    size = Size(headSize + px(0.08f), headSize + px(0.06f)),
                    cornerRadius = corner
                )
                // Diamond helmet highlight
                drawRoundRect(
                    color = Color(0xFF72EFDD),
                    topLeft = Offset(headLeft, headTop - px(0.04f)),
                    size = Size(headSize, px(0.14f)),
                    cornerRadius = CornerRadius(px(0.05f), px(0.05f))
                )
                // Face opening
                drawRoundRect(
                    color = Color(0xFFFFD8B0),
                    topLeft = Offset(headLeft + px(0.08f), headTop + headSize * 0.28f),
                    size = Size(headSize - px(0.16f), headSize * 0.56f),
                    cornerRadius = CornerRadius(px(0.04f), px(0.04f))
                )
                // Gold crest
                drawRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(cx - px(0.04f), headTop - px(0.14f)),
                    size = Size(px(0.08f), px(0.16f))
                )
                drawPixelEyesAndSmile(cx, headTop + headSize * 0.56f, px(0.05f), Color(0xFF1F2D3D))
            }

            "vx_slime" -> {
                // Cute glowing green bouncy cube slime
                drawRoundRect(
                    color = Color(0xFF2ECC71),
                    topLeft = Offset(headLeft - px(0.03f), headTop + px(0.02f)),
                    size = Size(headSize + px(0.06f), headSize),
                    cornerRadius = CornerRadius(px(0.12f), px(0.12f))
                )
                // Inner jelly core
                drawRoundRect(
                    color = Color(0xFF82E0AA),
                    topLeft = Offset(headLeft + px(0.06f), headTop + px(0.10f)),
                    size = Size(headSize - px(0.12f), headSize * 0.72f),
                    cornerRadius = CornerRadius(px(0.08f), px(0.08f))
                )
                // Glossy highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.75f),
                    topLeft = Offset(headLeft + px(0.06f), headTop + px(0.08f)),
                    size = Size(px(0.12f), px(0.06f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                // Cute wide blocky eyes
                drawRoundRect(
                    color = Color(0xFF145A32),
                    topLeft = Offset(cx - px(0.16f), headTop + headSize * 0.44f),
                    size = Size(px(0.09f), px(0.10f)),
                    cornerRadius = CornerRadius(px(0.02f), px(0.02f))
                )
                drawRoundRect(
                    color = Color(0xFF145A32),
                    topLeft = Offset(cx + px(0.07f), headTop + headSize * 0.44f),
                    size = Size(px(0.09f), px(0.10f)),
                    cornerRadius = CornerRadius(px(0.02f), px(0.02f))
                )
                drawRoundRect(
                    color = Color(0xFFFF8FA3),
                    topLeft = Offset(cx - px(0.06f), headTop + headSize * 0.64f),
                    size = Size(px(0.12f), px(0.06f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
            }

            "vx_cat" -> {
                // Blocky Kitty with pointy ears
                val earSize = px(0.16f)
                drawRoundRect(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(headLeft + px(0.02f), headTop - earSize * 0.6f),
                    size = Size(earSize, earSize),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                drawRoundRect(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(headLeft + headSize - earSize - px(0.02f), headTop - earSize * 0.6f),
                    size = Size(earSize, earSize),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                drawRoundRect(
                    color = Color(0xFFFFCC80),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headSize, headSize),
                    cornerRadius = corner
                )
                drawPixelEyesAndSmile(cx, headTop + headSize * 0.54f, px(0.055f), Color(0xFF4E342E))
                // Pink kitty nose
                drawCircle(Color(0xFFFF6B8B), radius = px(0.035f), center = Offset(cx, headTop + headSize * 0.64f))
            }

            "vx_robot" -> {
                // Pixel Robot with antenna
                drawRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(cx - px(0.025f), headTop - px(0.16f)),
                    size = Size(px(0.05f), px(0.16f))
                )
                drawCircle(Color(0xFFFFD54F), radius = px(0.055f), center = Offset(cx, headTop - px(0.16f)))
                drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headSize, headSize),
                    cornerRadius = corner
                )
                // Dark LED screen face
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(headLeft + px(0.07f), headTop + px(0.10f)),
                    size = Size(headSize - px(0.14f), headSize - px(0.20f)),
                    cornerRadius = CornerRadius(px(0.05f), px(0.05f))
                )
                // Glowing cyan pixel eyes
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(cx - px(0.14f), headTop + headSize * 0.38f),
                    size = Size(px(0.09f), px(0.09f)),
                    cornerRadius = CornerRadius(px(0.02f), px(0.02f))
                )
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(cx + px(0.05f), headTop + headSize * 0.38f),
                    size = Size(px(0.09f), px(0.09f)),
                    cornerRadius = CornerRadius(px(0.02f), px(0.02f))
                )
                drawRoundRect(
                    color = Color(0xFF4ADE80),
                    topLeft = Offset(cx - px(0.09f), headTop + headSize * 0.64f),
                    size = Size(px(0.18f), px(0.05f)),
                    cornerRadius = CornerRadius(px(0.02f), px(0.02f))
                )
            }

            "vx_wizard" -> {
                // Starry Cube Wizard
                drawRoundRect(
                    color = Color(0xFFFFD8B0),
                    topLeft = Offset(headLeft, headTop + px(0.04f)),
                    size = Size(headSize, headSize * 0.92f),
                    cornerRadius = corner
                )
                // Wizard hat brim & cone
                drawRoundRect(
                    color = Color(0xFF7C3AED),
                    topLeft = Offset(headLeft - px(0.07f), headTop + px(0.04f)),
                    size = Size(headSize + px(0.14f), px(0.09f)),
                    cornerRadius = CornerRadius(px(0.04f), px(0.04f))
                )
                val hatPath = Path().apply {
                    moveTo(cx - px(0.20f), headTop + px(0.05f))
                    lineTo(cx, headTop - px(0.22f))
                    lineTo(cx + px(0.20f), headTop + px(0.05f))
                    close()
                }
                drawPath(hatPath, color = Color(0xFF8B5CF6))
                drawCircle(Color(0xFFFFD54F), radius = px(0.045f), center = Offset(cx, headTop - px(0.05f)))
                drawPixelEyesAndSmile(cx, headTop + headSize * 0.56f, px(0.05f), Color(0xFF311B92))
            }

            else -> {
                // vx_pirate: Blocky Pirate King with bandana & crown
                drawRoundRect(
                    color = Color(0xFFFFD8B0),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headSize, headSize),
                    cornerRadius = corner
                )
                // Red bandana
                drawRoundRect(
                    color = Color(0xFFE11D48),
                    topLeft = Offset(headLeft - px(0.02f), headTop - px(0.04f)),
                    size = Size(headSize + px(0.04f), px(0.18f)),
                    cornerRadius = CornerRadius(px(0.05f), px(0.05f))
                )
                // Mini gold crown
                drawRoundRect(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(cx - px(0.12f), headTop - px(0.14f)),
                    size = Size(px(0.24f), px(0.10f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                // Eye patch on left eye + bright right eye
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(cx - px(0.17f), headTop + headSize * 0.42f),
                    size = Size(px(0.12f), px(0.10f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
                drawCircle(
                    color = Color(0xFF1F2D3D),
                    radius = px(0.048f),
                    center = Offset(cx + px(0.11f), headTop + headSize * 0.48f)
                )
                drawRoundRect(
                    color = Color(0xFF1F2D3D),
                    topLeft = Offset(cx - px(0.08f), headTop + headSize * 0.70f),
                    size = Size(px(0.16f), px(0.055f)),
                    cornerRadius = CornerRadius(px(0.03f), px(0.03f))
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPixelEyesAndSmile(
    cx: Float,
    eyeCenterY: Float,
    eyeRadius: Float,
    eyeColor: Color
) {
    val eyeOffsetX = eyeRadius * 2.3f
    drawRoundRect(
        color = eyeColor,
        topLeft = Offset(cx - eyeOffsetX - eyeRadius, eyeCenterY - eyeRadius),
        size = Size(eyeRadius * 2f, eyeRadius * 2.2f),
        cornerRadius = CornerRadius(eyeRadius * 0.4f, eyeRadius * 0.4f)
    )
    drawRoundRect(
        color = eyeColor,
        topLeft = Offset(cx + eyeOffsetX - eyeRadius, eyeCenterY - eyeRadius),
        size = Size(eyeRadius * 2f, eyeRadius * 2.2f),
        cornerRadius = CornerRadius(eyeRadius * 0.4f, eyeRadius * 0.4f)
    )
    // Rosy pixel cheeks
    drawCircle(
        color = Color(0xFFFF8FA3).copy(alpha = 0.7f),
        radius = eyeRadius * 0.9f,
        center = Offset(cx - eyeOffsetX * 1.45f, eyeCenterY + eyeRadius * 1.8f)
    )
    drawCircle(
        color = Color(0xFFFF8FA3).copy(alpha = 0.7f),
        radius = eyeRadius * 0.9f,
        center = Offset(cx + eyeOffsetX * 1.45f, eyeCenterY + eyeRadius * 1.8f)
    )
    // Cute blocky smile
    drawRoundRect(
        color = eyeColor,
        topLeft = Offset(cx - eyeRadius * 1.3f, eyeCenterY + eyeRadius * 2.0f),
        size = Size(eyeRadius * 2.6f, eyeRadius * 0.85f),
        cornerRadius = CornerRadius(eyeRadius * 0.4f, eyeRadius * 0.4f)
    )
}
