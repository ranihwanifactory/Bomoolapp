package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ar.ArSensorController
import com.example.ar.ArTreasureEngine
import com.example.ar.SpawnedTreasure
import com.example.audio.SoundEffectsManager
import com.example.haptics.HapticsHelper
import com.example.model.TreasureDefinition
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.RarityBadge
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark
import com.example.ui.theme.SunnyGoldLight
import com.example.viewmodel.RoundSummary
import com.example.viewmodel.TreasureAppState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xFrac: Float,
    val yFrac: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun ArHuntScreen(
    state: TreasureAppState,
    soundManager: SoundEffectsManager,
    haptics: HapticsHelper,
    onRoundComplete: (List<TreasureDefinition>) -> Unit,
    onPlayAgain: () -> Unit,
    onExitToHome: () -> Unit
) {
    BackHandler { onExitToHome() }

    val context = LocalContext.current
    val profile = state.activeProfile ?: return

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraAvailable by remember { mutableStateOf(true) }
    var forceVirtualGarden by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            forceVirtualGarden = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val sensorController = remember(context) { ArSensorController(context) }
    DisposableEffect(sensorController) {
        sensorController.start()
        onDispose { sensorController.stop() }
    }
    val orientation by sensorController.orientation.collectAsStateWithLifecycle()

    val equippedItems = remember(state.items, state.equippedItemIds) {
        state.items.filter { it.id in state.equippedItemIds }
    }
    val magnifierScale = remember(equippedItems) { ArTreasureEngine.computeMagnifierScale(equippedItems) }
    val radarRange = remember(equippedItems) { ArTreasureEngine.computeRadarRangeMeters(equippedItems) }
    val initialSeconds = remember(equippedItems) { ArTreasureEngine.computeRoundDurationSeconds(equippedItems) }

    var roundKey by remember { mutableIntStateOf(0) }
    var spawnedTreasures by remember(roundKey) {
        mutableStateOf(
            ArTreasureEngine.spawnRoundTreasures(
                playerLevel = profile.level,
                allTreasures = state.treasures,
                rarityConfigs = state.rarityConfigs,
                equippedItems = equippedItems
            )
        )
    }
    val collectedThisRound = remember(roundKey) { mutableStateListOf<TreasureDefinition>() }
    var secondsLeft by remember(roundKey) { mutableIntStateOf(initialSeconds) }
    var roundEnded by remember(roundKey) { mutableStateOf(false) }
    var showSafetyBanner by remember { mutableStateOf(true) }
    var captureBannerText by remember { mutableStateOf<String?>(null) }
    var lockTargetId by remember { mutableStateOf<String?>(null) }
    var lockProgress by remember { mutableFloatStateOf(0f) }
    var confettiParticles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    fun triggerCapture(target: SpawnedTreasure) {
        if (target.collected || roundEnded) return
        spawnedTreasures = spawnedTreasures.map {
            if (it.instanceId == target.instanceId) it.copy(collected = true) else it
        }
        collectedThisRound.add(target.definition)
        lockProgress = 0f
        lockTargetId = null

        val isHigh = target.definition.rarityEnum.sortOrder >= 2
        soundManager.playCollectSparkle(isHigh)
        haptics.collect()
        captureBannerText = "${target.definition.emoji} ${target.definition.name} 획득! (+${target.definition.coins}코인)"

        val colors = listOf(SunnyGold, MintEmerald, CoralBerry, SkyBluePrimary, Color(0xFF9B5DE5))
        confettiParticles = List(26) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 0.025f + 0.008f
            ConfettiParticle(
                xFrac = 0.5f,
                yFrac = 0.5f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 0.01f,
                color = colors.random(),
                radius = Random.nextFloat() * 8f + 5f
            )
        }
    }

    // Countdown timer
    LaunchedEffect(roundKey, roundEnded) {
        if (roundEnded) return@LaunchedEffect
        while (secondsLeft > 0 && !roundEnded) {
            delay(1000L)
            secondsLeft -= 1
            if (spawnedTreasures.all { it.collected }) {
                break
            }
        }
        if (!roundEnded) {
            roundEnded = true
            soundManager.playRoundFinish()
            onRoundComplete(collectedThisRound.toList())
        }
    }

    // Check if all treasures collected early
    LaunchedEffect(spawnedTreasures) {
        if (!roundEnded && spawnedTreasures.isNotEmpty() && spawnedTreasures.all { it.collected }) {
            delay(600L)
            roundEnded = true
            soundManager.playRoundFinish()
            onRoundComplete(collectedThisRound.toList())
        }
    }

    // Reticle auto-lock timer
    LaunchedEffect(orientation, spawnedTreasures, roundEnded) {
        if (roundEnded) return@LaunchedEffect
        val projected = spawnedTreasures
            .filter { !it.collected }
            .map { ArTreasureEngine.projectTreasure(it, orientation, magnifierScale) }
        val aimed = projected.firstOrNull { it.isNearReticle }
        if (aimed == null) {
            lockTargetId = null
            lockProgress = 0f
        } else {
            if (lockTargetId != aimed.treasure.instanceId) {
                lockTargetId = aimed.treasure.instanceId
                lockProgress = 0.1f
                soundManager.playRadarPing()
            }
        }
    }

    LaunchedEffect(lockTargetId, roundEnded) {
        val targetId = lockTargetId ?: return@LaunchedEffect
        while (lockTargetId == targetId && !roundEnded) {
            delay(60L)
            lockProgress = (lockProgress + 0.08f).coerceAtMost(1f)
            if (lockProgress >= 1f) {
                val target = spawnedTreasures.firstOrNull { it.instanceId == targetId && !it.collected }
                if (target != null) {
                    triggerCapture(target)
                }
                break
            }
        }
    }

    // Confetti & banner fade loops
    LaunchedEffect(confettiParticles.isNotEmpty()) {
        if (confettiParticles.isEmpty()) return@LaunchedEffect
        repeat(24) {
            delay(30L)
            confettiParticles = confettiParticles.map { p ->
                p.copy(xFrac = p.xFrac + p.vx, yFrac = p.yFrac + p.vy, vy = p.vy + 0.0015f)
            }
        }
        confettiParticles = emptyList()
    }

    LaunchedEffect(captureBannerText) {
        if (captureBannerText != null) {
            delay(2000L)
            captureBannerText = null
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "treasure_pulse")
    val bobFloat by pulseTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_treasure"
    )
    val radarSweep by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    val useLiveCamera = hasCameraPermission && cameraAvailable && !forceVirtualGarden
    val projectedList = remember(spawnedTreasures, orientation, magnifierScale) {
        spawnedTreasures.filter { !it.collected }
            .map { ArTreasureEngine.projectTreasure(it, orientation, magnifierScale) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Smooth touch-pan fallback for emulators or stationary play
                    sensorController.addManualPan(
                        deltaAzimuthDeg = -dragAmount.x * 0.14f,
                        deltaPitchDeg = dragAmount.y * 0.10f
                    )
                }
            }
    ) {
        // 1. CameraX Preview OR Cartoon Virtual Garden Fallback
        if (useLiveCamera) {
            CameraPreviewBackground(
                onCameraUnavailable = { cameraAvailable = false }
            )
        } else {
            val parallaxX = ((orientation.azimuthDeg % 360f) / 360f - 0.5f) * 60f
            val parallaxY = (orientation.pitchDeg / 65f) * 40f
            Image(
                painter = painterResource(id = R.drawable.img_voxel_park),
                contentDescription = "가상 블록 공원 배경 모드",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.18f)
                    .offset { IntOffset(parallaxX.roundToInt(), parallaxY.roundToInt()) }
            )
        }

        // 2. AR Treasure Nodes & Off-screen Directional Arrows
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            projectedList.forEach { proj ->
                if (proj.isVisibleOnScreen) {
                    val rarityColor = proj.treasure.definition.rarityEnum.badgeColor
                    val sizeDp = (82f * proj.visualScale).coerceIn(58f, 136f).dp
                    val xPx = proj.normX * widthPx
                    val yPx = proj.normY * heightPx

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (xPx - sizeDp.toPx() / 2f).roundToInt(),
                                    y = (yPx - sizeDp.toPx() / 2f + bobFloat).roundToInt()
                                )
                            }
                            .size(sizeDp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        rarityColor.copy(alpha = 0.50f),
                                        rarityColor.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .clickable { triggerCapture(proj.treasure) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = proj.treasure.definition.emoji,
                                fontSize = (38f * proj.visualScale).coerceIn(28f, 58f).sp
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = proj.treasure.definition.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Edge directional indicator arrow
                    val rad = Math.toRadians(proj.offscreenAngleDeg.toDouble())
                    val edgeX = (0.5f + 0.42f * cos(rad).toFloat()).coerceIn(0.08f, 0.92f) * widthPx
                    val edgeY = (0.5f + 0.36f * sin(rad).toFloat()).coerceIn(0.18f, 0.80f) * heightPx
                    Surface(
                        color = proj.treasure.definition.rarityEnum.badgeColor.copy(alpha = 0.92f),
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .offset { IntOffset((edgeX - 22.dp.toPx()).roundToInt(), (edgeY - 22.dp.toPx()).roundToInt()) }
                            .size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "➤",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier.rotate(proj.offscreenAngleDeg)
                            )
                        }
                    }
                }
            }
        }

        // 3. Center Aiming Reticle with Lock-On Progress
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            if (lockProgress > 0f) {
                CircularProgressIndicator(
                    progress = { lockProgress },
                    modifier = Modifier.size(86.dp),
                    color = SunnyGold,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    strokeWidth = 6.dp
                )
            }
            Canvas(modifier = Modifier.size(62.dp)) {
                drawCircle(
                    color = if (lockTargetId != null) SunnyGold else Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = if (lockTargetId != null) SunnyGold else Color.White,
                    radius = 5f
                )
            }
        }

        // 4. Confetti Canvas
        if (confettiParticles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiParticles.forEach { p ->
                    drawCircle(
                        color = p.color,
                        radius = p.radius,
                        center = Offset(p.xFrac * size.width, p.yFrac * size.height)
                    )
                }
            }
        }

        // 5. Top HUD & Safety Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.62f),
                    shape = CircleShape
                ) {
                    IconButton(
                        onClick = onExitToHome,
                        modifier = Modifier.testTag("ar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "나가기",
                            tint = Color.White
                        )
                    }
                }

                Surface(
                    color = if (secondsLeft <= 15) CoralBerry else Color.Black.copy(alpha = 0.68f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "⏱️ ${secondsLeft}초  |  🎁 ${collectedThisRound.size}/${spawnedTreasures.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.62f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable {
                        forceVirtualGarden = !forceVirtualGarden
                    }
                ) {
                    Text(
                        text = if (useLiveCamera) "📷 AR" else "🧱 블록공원",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showSafetyBanner) {
                Surface(
                    color = SunnyGoldLight.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ 주변을 잘 살피고, 걸어 다니면서 하지 마세요!\n🔒 카메라 영상은 저장되지 않아요.",
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepOceanNavy,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showSafetyBanner = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "안내 닫기", tint = DeepOceanNavy)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = captureBannerText != null) {
                Surface(
                    color = MintEmerald,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = captureBannerText.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 6. Bottom Controls: Hint + Radar Minimap
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (equippedItems.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        equippedItems.forEach { item ->
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${item.emoji} ${item.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SunnyGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.60f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable { sensorController.resetPan() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "시선 초기화",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (orientation.hasHardwareSensor) "폰을 돌리거나 화면을 드래그!" else "화면을 드래그해 주변을 둘러봐요!",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Circular Radar Minimap
            Surface(
                color = Color(0xCC0D2B45),
                shape = CircleShape,
                border = BorderStroke(3.dp, SkyBluePrimary),
                modifier = Modifier.size(104.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension / 2f - 6f
                    drawCircle(Color.White.copy(alpha = 0.18f), radius = maxR * 0.5f, style = Stroke(2f))
                    drawCircle(Color.White.copy(alpha = 0.25f), radius = maxR, style = Stroke(2f))

                    // Facing cone
                    val conePath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - maxR * 0.45f, center.y - maxR * 0.85f)
                        lineTo(center.x + maxR * 0.45f, center.y - maxR * 0.85f)
                        close()
                    }
                    drawPath(conePath, color = SkyBluePrimary.copy(alpha = 0.28f))

                    // Sweeping radar line
                    val sweepRad = Math.toRadians(radarSweep.toDouble())
                    drawLine(
                        color = MintEmerald.copy(alpha = 0.6f),
                        start = center,
                        end = Offset(
                            center.x + (cos(sweepRad) * maxR).toFloat(),
                            center.y + (sin(sweepRad) * maxR).toFloat()
                        ),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Blips for uncollected treasures
                    spawnedTreasures.filter { !it.collected }.forEach { t ->
                        val (rx, ry) = ArTreasureEngine.radarOffset(t, orientation, radarRange)
                        drawCircle(
                            color = t.definition.rarityEnum.badgeColor,
                            radius = 6f,
                            center = Offset(center.x + rx * maxR, center.y + ry * maxR)
                        )
                    }
                    // Center player dot
                    drawCircle(Color.White, radius = 5f, center = center)
                }
            }
        }

        // 7. Round Result Overlay
        if (roundEnded) {
            RoundResultOverlay(
                foundTreasures = collectedThisRound,
                summary = state.lastRoundSummary,
                onPlayAgain = {
                    roundKey += 1
                    onPlayAgain()
                },
                onBackHome = onExitToHome
            )
        }
    }
}

@Composable
private fun CameraPreviewBackground(
    onCameraUnavailable: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        future.addListener({
            try {
                val cameraProvider = future.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.unbindAll()
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                } else {
                    onCameraUnavailable()
                }
            } catch (_: Exception) {
                onCameraUnavailable()
            }
        }, executor)

        onDispose {
            try {
                future.get().unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun RoundResultOverlay(
    foundTreasures: List<TreasureDefinition>,
    summary: RoundSummary?,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎊 탐험 완료! 🎊", style = MaterialTheme.typography.headlineLarge, color = DeepOceanNavy)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "이번 판에서 보물 ${foundTreasures.size}개를 찾았어요!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SkyBluePrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (foundTreasures.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        foundTreasures.take(5).forEach { t ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = t.emoji, fontSize = 30.sp)
                                RarityBadge(rarity = t.rarityEnum)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "아쉬워요! 다음 판엔 레이더를 보고 꼭 찾아봐요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = SunnyGoldLight,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🪙 코인", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "+${summary?.earnedCoins ?: foundTreasures.sumOf { it.coins }}",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepOceanNavy
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐ 경험치", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "+${summary?.earnedExp ?: foundTreasures.sumOf { (it.score * 0.8).toLong() }}",
                                style = MaterialTheme.typography.titleLarge,
                                color = SkyBluePrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 점수", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "+${summary?.earnedScore ?: foundTreasures.sumOf { it.score }}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MintEmeraldDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ChunkyActionButton(
                        text = "홈으로",
                        containerColor = Color(0xFFE6F3FC),
                        shadowColor = Color(0xFFB5D8F0),
                        contentColor = DeepOceanNavy,
                        modifier = Modifier.weight(1f),
                        onClick = onBackHome
                    )
                    ChunkyActionButton(
                        text = "한 판 더!",
                        emoji = "🧭",
                        containerColor = MintEmerald,
                        shadowColor = MintEmeraldDark,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1.3f),
                        onClick = onPlayAgain
                    )
                }
            }
        }
    }
}
