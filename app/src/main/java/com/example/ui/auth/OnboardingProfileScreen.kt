package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DefaultGameCatalog
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.SkyCloudsBackground
import com.example.ui.components.VoxelCharacterAvatar
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileScreen(
    onSaveProfile: (nickname: String, avatar: String) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf(DefaultGameCatalog.avatars.first().id) }
    val selectedOption = DefaultGameCatalog.avatars.firstOrNull { it.id == selectedAvatarId }
        ?: DefaultGameCatalog.avatars.first()

    SkyCloudsBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "반가워요, 새 탐험가님!",
                style = MaterialTheme.typography.headlineLarge,
                color = DeepOceanNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "멋진 별명과 함께 모험할 친구를 골라 주세요.",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftSlateText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "1. 탐험가 별명 (2~12글자)",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { if (it.length <= 14) nickname = it },
                        placeholder = { Text("예: 번개여우, 보물왕") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = Color(0xFFB3D9F2)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_nickname_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "2. 내 캐릭터 친구 고르기",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanNavy
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 4
                    ) {
                        DefaultGameCatalog.avatars.forEach { option ->
                            val selected = option.id == selectedAvatarId
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) SkyBlueSurface else Color(0xFFF6FBFF),
                                border = BorderStroke(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) SunnyGold else Color(0xFFD5EAF8)
                                ),
                                modifier = Modifier
                                    .size(72.dp)
                                    .clickable { selectedAvatarId = option.id }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    VoxelCharacterAvatar(
                                        avatarKey = option.id,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = option.name.take(5),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = DeepOceanNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ChunkyActionButton(
                text = "보물 모험 출발하기!",
                emoji = selectedOption.badgeEmoji,
                containerColor = MintEmerald,
                shadowColor = MintEmeraldDark,
                contentColor = Color.White,
                testTagName = "save_onboarding_button",
                enabled = nickname.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onSaveProfile(nickname.trim(), selectedAvatarId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
