package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DefaultGameCatalog
import com.example.model.ItemDefinition
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.CoinPill
import com.example.ui.components.LevelExpBar
import com.example.ui.components.VoxelCharacterAvatar
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MagicLavender
import com.example.ui.theme.MagicLavenderLight
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark
import com.example.ui.theme.SunnyGoldLight
import com.example.viewmodel.TreasureAppState

@Composable
fun HomeScreen(
    state: TreasureAppState,
    onStartHunt: () -> Unit,
    onToggleEquip: (String) -> Unit,
    onBuyItem: (ItemDefinition) -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.activeProfile ?: return
    val needExp = DefaultGameCatalog.needExpForLevel(profile.level, state.levelConfigs)
    val activeItems = state.items.filter { it.active }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with App Title, Bell Badge, and Coins
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧭", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "보물 레이더",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepOceanNavy
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CoinPill(coins = profile.coins)
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 2.dp
                    ) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("notifications_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadNotificationCount > 0) {
                                        Badge(containerColor = CoralBerry) {
                                            Text(
                                                text = state.unreadNotificationCount.toString(),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "알림함 열기",
                                    tint = SkyBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Event Banner
        state.activeEvent?.let { event ->
            item {
                Surface(
                    color = MagicLavenderLight,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, MagicLavender.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${event.title} (${event.multiplierPercent / 100.0}배!)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MagicLavender
                            )
                            Text(
                                text = event.desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepOceanNavy
                            )
                        }
                    }
                }
            }
        }

        // Player Profile & Mascot Hero Card
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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
                                text = "${profile.nickname}님, 안녕!",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepOceanNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "모은 보물 ${profile.treasureCount}개 · 점수 ${profile.score}점",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftSlateText
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.img_voxel_mascot),
                            contentDescription = "안내 마스코트",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LevelExpBar(
                        level = profile.level,
                        currentExp = profile.exp,
                        needExp = needExp
                    )

                    if (profile.banned) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFFFFE5E5),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ 현재 계정이 일시 제한되어 있어요. 관리자에게 문의해 주세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoralBerry,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Big Chunky Play CTA
        item {
            ChunkyActionButton(
                text = "보물 찾으러 가기!",
                emoji = "📸",
                containerColor = SunnyGold,
                shadowColor = SunnyGoldDark,
                contentColor = DeepOceanNavy,
                enabled = !profile.banned,
                testTagName = "start_hunt_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                onClick = onStartHunt
            )
        }

        // Pre-Round Item Loadout (max 2)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎒 출발 전 아이템 장착 (${state.equippedItemIds.size}/2)",
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepOceanNavy
                    )
                    Text(
                        text = "최대 2개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftSlateText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeItems, key = { it.id }) { item ->
                        val ownedCount = state.inventoryMap[item.id] ?: 0L
                        val equipped = item.id in state.equippedItemIds
                        Surface(
                            color = if (equipped) SunnyGoldLight else Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = if (equipped) 2.5.dp else 1.dp,
                                color = if (equipped) SunnyGoldDark else Color(0xFFD6EAF8)
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .width(136.dp)
                                .clickable { onToggleEquip(item.id) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = item.emoji, fontSize = 30.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    color = DeepOceanNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (equipped) "장착 중! (보유 $ownedCount)" else "보유: ${ownedCount}개",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (equipped) SunnyGoldDark else SoftSlateText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Item Shop Header
        item {
            Text(
                text = "🛒 탐험 도구 상점",
                style = MaterialTheme.typography.titleLarge,
                color = DeepOceanNavy
            )
        }

        items(activeItems, key = { "shop_${it.id}" }) { item ->
            val owned = state.inventoryMap[item.id] ?: 0L
            val unlocked = profile.level >= item.minLevel
            val canAfford = profile.coins >= item.price
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SkyBlueSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = item.emoji, fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepOceanNavy
                            )
                            if (owned > 0L) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = SkyBlueSurface, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "${owned}개 보유",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SkyBluePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftSlateText
                        )
                        if (!unlocked) {
                            Text(
                                text = "🔒 Lv.${item.minLevel}부터 구매 가능",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoralBerry
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ChunkyActionButton(
                        text = "🪙 ${item.price}",
                        containerColor = if (unlocked && canAfford) MintEmerald else Color(0xFFDCE6EE),
                        shadowColor = if (unlocked && canAfford) MintEmeraldDark else Color(0xFFB8C7D4),
                        contentColor = if (unlocked && canAfford) Color.White else SoftSlateText,
                        enabled = unlocked && canAfford && !profile.banned,
                        modifier = Modifier.width(96.dp),
                        onClick = { onBuyItem(item) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
