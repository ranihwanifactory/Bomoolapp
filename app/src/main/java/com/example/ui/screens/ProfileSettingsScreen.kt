package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.model.DefaultGameCatalog
import com.example.ui.auth.signOutFromGoogle
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.GuardianPrivacyDialog
import com.example.ui.components.VoxelCharacterAvatar
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.CoralBerryDark
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MagicLavender
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.viewmodel.TreasureAppState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    state: TreasureAppState,
    onUpdateProfile: (String, String) -> Unit,
    onSetSound: (Boolean) -> Unit,
    onSetVibration: (Boolean) -> Unit,
    onShowTutorial: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.activeProfile ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "⚙️ 내 정보 및 설정",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepOceanNavy
            )
        }

        // Explorer Profile Card
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SkyBlueSurface,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            VoxelCharacterAvatar(
                                avatarKey = profile.avatar,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.nickname,
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepOceanNavy
                        )
                        Text(
                            text = "Lv.${profile.level} · 코인 ${profile.coins} · 보물 ${profile.treasureCount}개",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftSlateText
                        )
                    }
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("꾸미기")
                    }
                }
            }
        }

        // Sound & Vibration Settings
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔊 효과음", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                            Text("반짝임·레벨업 팡파르 소리 켜기/끄기", style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                        }
                        Switch(
                            checked = state.localSettings.soundEnabled,
                            onCheckedChange = onSetSound
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📳 진동 반응", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                            Text("버튼 터치·보물 획득 시 진동 느끼기", style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                        }
                        Switch(
                            checked = state.localSettings.vibrationEnabled,
                            onCheckedChange = onSetVibration
                        )
                    }
                }
            }
        }

        // Tutorial & Child Safety / Privacy
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShowTutorial() }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖 AR 보물찾기 사용법 다시 보기", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text("열기 >", color = SkyBluePrimary)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛡️ 개인정보처리방침 및 보호자 안내", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text("보기 >", color = SkyBluePrimary)
                    }
                }
            }
        }

        // Admin Dashboard Entry (visible ONLY to acehwan69@gmail.com)
        if (state.isAdmin) {
            item {
                ChunkyActionButton(
                    text = "관리자 대시보드 열기",
                    emoji = "👑",
                    containerColor = MagicLavender,
                    shadowColor = Color(0xFF6B2CB8),
                    contentColor = Color.White,
                    testTagName = "open_admin_dashboard_button",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenAdminDashboard
                )
            }
        }

        item {
            ChunkyActionButton(
                text = "로그아웃",
                emoji = "👋",
                containerColor = CoralBerry,
                shadowColor = CoralBerryDark,
                contentColor = Color.White,
                testTagName = "sign_out_button",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    signOutFromGoogle(credentialManager, scope)
                }
            )
        }
    }

    if (showEditDialog) {
        var newNick by remember { mutableStateOf(profile.nickname) }
        var newAvatar by remember { mutableStateOf(profile.avatar) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("탐험가 꾸미기", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newNick,
                        onValueChange = { if (it.length <= 14) newNick = it },
                        label = { Text("새 별명") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4
                    ) {
                        DefaultGameCatalog.avatars.forEach { opt ->
                            val selected = opt.id == newAvatar
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) SkyBlueSurface else Color(0xFFF6FAFD),
                                border = BorderStroke(if (selected) 2.5.dp else 1.dp, if (selected) SunnyGold else Color(0xFFD5EAF8)),
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable { newAvatar = opt.id }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(3.dp)
                                ) {
                                    VoxelCharacterAvatar(
                                        avatarKey = opt.id,
                                        modifier = Modifier.size(36.dp),
                                        fallbackFontSize = 22.dp
                                    )
                                    Text(
                                        text = opt.name.take(4),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                        color = DeepOceanNavy
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNick.trim().isNotEmpty()) {
                            onUpdateProfile(newNick.trim(), newAvatar)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("취소") }
            }
        )
    }

    if (showPrivacyDialog) {
        GuardianPrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }
}
