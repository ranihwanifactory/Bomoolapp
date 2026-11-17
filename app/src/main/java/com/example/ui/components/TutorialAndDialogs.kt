package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppNotification
import com.example.model.ItemDefinition
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MagicLavender
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark
import com.example.ui.theme.SunnyGoldLight

@Composable
fun TutorialOverlayDialog(
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            Triple("🧱", "안녕! 나는 큐브 탐험가야!", "스마트폰 카메라를 들고\n주변을 둘러보며 숨은 보물을 찾아보자!"),
            Triple("🧭", "화살표와 레이더를 확인해!", "화면 밖에 보물이 있으면 가장자리에 화살표가 뜨고,\n오른쪽 아래 레이더가 방향을 알려줘요!"),
            Triple("🎯", "가운데 동그라미를 맞춰봐!", "반짝이는 보물에 조준점을 잠시 맞추거나\n손가락으로 톡! 누르면 보물이 내 품으로 쏙!"),
            Triple("🛡️", "안전이 제일 중요해요!", "걸어 다니지 말고 꼭 멈춰 서서 주변을 살피며 플레이해요.\n카메라 영상은 어디에도 저장되지 않아요!")
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]

    Dialog(onDismissRequest = onFinish) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = SkyBlueSurface,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (page == 0) {
                            Image(
                                painter = painterResource(id = R.drawable.img_voxel_mascot),
                                contentDescription = "큐브 탐험가 마스코트",
                                modifier = Modifier.clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = current.first, fontSize = 48.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = current.second,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepOceanNavy,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = current.third,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftSlateText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == page) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (idx == page) SkyBluePrimary else Color(0xFFD0E5F5))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (page > 0) {
                        TextButton(
                            onClick = { page -= 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("이전")
                        }
                    }
                    ChunkyActionButton(
                        text = if (page == pages.lastIndex) "모험 시작!" else "다음 (${page + 1}/${pages.size})",
                        containerColor = if (page == pages.lastIndex) MintEmerald else SunnyGold,
                        shadowColor = if (page == pages.lastIndex) MintEmeraldDark else SunnyGoldDark,
                        contentColor = if (page == pages.lastIndex) Color.White else DeepOceanNavy,
                        modifier = Modifier.weight(2f),
                        onClick = {
                            if (page < pages.lastIndex) page += 1 else onFinish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelUpCelebrationDialog(
    newLevel: Long,
    rewardItemNames: List<String>,
    onDismiss: () -> Unit
) {
    val unlockedHint = when {
        newLevel >= 5L -> "전설 보물상자 확률 UP!"
        newLevel >= 3L -> "영웅 보물 '요정의 왕관' 출현 가능!"
        newLevel >= 2L -> "고급·희귀 보물이 더 자주 나타나요!"
        else -> "멋진 탐험가가 되었어요!"
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉✨🏆✨🎉", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "레벨 업!! Lv.$newLevel",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MagicLavender
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SunnyGoldLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = unlockedHint,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanNavy,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                if (rewardItemNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🎁 레벨업 선물: ${rewardItemNames.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MintEmeraldDark,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                ChunkyActionButton(
                    text = "신난다! 확인",
                    emoji = "🌟",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<AppNotification>,
    readIds: Set<String>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔔 탐험대 알림함",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepOceanNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "닫기")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (notifications.isEmpty()) {
                Surface(
                    color = SkyBlueSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "아직 도착한 새 소식이 없어요!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftSlateText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(340.dp)
                ) {
                    items(notifications, key = { it.id }) { noti ->
                        val isRead = noti.id in readIds
                        Surface(
                            color = if (isRead) Color(0xFFF6FAFD) else SunnyGoldLight,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkRead(noti.id) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = noti.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepOceanNavy
                                    )
                                    if (!isRead) {
                                        Surface(color = CoralBerry, shape = CircleShape) {
                                            Text(
                                                text = "NEW",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = noti.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftSlateText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianPrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🛡️ 어린이 안전 및 개인정보 안내",
                style = MaterialTheme.typography.titleLarge,
                color = DeepOceanNavy
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. 카메라 영상은 기기 화면에만 표시되며, 서버로 전송되거나 사진/동영상으로 저장되지 않습니다.")
                Text("2. GPS 위치 정보는 절대 수집하지 않으며, 채팅 기능도 없습니다.")
                Text("3. 랭킹에는 내가 정한 별명과 캐릭터 아이콘만 표시되고, 이메일 등 개인정보는 공개되지 않습니다.")
                Text("4. 광고, 외부 링크, 유료 결제가 없는 안전한 어린이용 게임입니다.")
                Text("5. 보호자분께서는 어린이가 보행 중이 아닌 안전한 실내외 정지 상태에서 즐길 수 있도록 지도해 주세요.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인했어요")
            }
        }
    )
}
