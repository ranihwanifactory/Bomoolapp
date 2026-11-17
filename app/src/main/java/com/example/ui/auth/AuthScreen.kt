package com.example.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.SkyCloudsBackground
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.CoralBerryLight
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    val bobTransition = rememberInfiniteTransition(label = "mascot_bob")
    val bobOffset by bobTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_y"
    )

    SkyCloudsBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "✨ 증강현실 보물찾기 모험 ✨",
                    style = MaterialTheme.typography.titleMedium,
                    color = SkyBluePrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "보물 레이더",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                color = DeepOceanNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "카메라로 우리 주변에 숨은\n반짝이는 보물을 찾아보아요!",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftSlateText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .size(190.dp)
                    .offset(y = bobOffset.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_voxel_mascot),
                    contentDescription = "블록 탐험가 캐릭터 마스코트",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Surface(
                    color = CoralBerryLight,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoralBerry,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = SkyBluePrimary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "구글 계정으로 문을 여는 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftSlateText
                )
            } else {
                ChunkyActionButton(
                    text = "구글로 시작하기",
                    emoji = "🚀",
                    containerColor = SunnyGold,
                    shadowColor = SunnyGoldDark,
                    contentColor = DeepOceanNavy,
                    testTagName = "google_sign_in_button",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "🔒 카메라 영상과 위치 정보는 절대 저장하거나 밖으로 보내지 않아요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftSlateText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
