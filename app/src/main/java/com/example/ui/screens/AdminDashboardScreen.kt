package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameEvent
import com.example.model.ItemDefinition
import com.example.model.LevelConfig
import com.example.model.RarityConfig
import com.example.model.TreasureDefinition
import com.example.model.UserProfile
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.RarityBadge
import com.example.ui.components.SkyCloudsBackground
import com.example.ui.components.VoxelCharacterAvatar
import com.example.ui.theme.CoralBerry
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MagicLavender
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGoldLight
import com.example.viewmodel.TreasureAppState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AdminTab(val title: String, val emoji: String) {
    STATS("통계 요약", "📊"),
    LEVELS("레벨 설정", "📈"),
    TREASURES("보물 관리", "💎"),
    ITEMS("아이템 관리", "🔍"),
    NOTIFICATIONS("알림 보내기", "🔔"),
    USERS("회원 관리", "👥"),
    EVENTS("이벤트", "🎉")
}

@Composable
fun AdminDashboardScreen(
    state: TreasureAppState,
    adminUsers: List<UserProfile>,
    onBack: () -> Unit,
    onSeedDefaults: () -> Unit,
    onSaveTreasure: (TreasureDefinition, Boolean) -> Unit,
    onDeleteTreasure: (String) -> Unit,
    onSaveItem: (ItemDefinition, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onSaveLevelConfig: (LevelConfig) -> Unit,
    onSaveRarityConfig: (RarityConfig) -> Unit,
    onSendNotification: (String, String, Long) -> Unit,
    onSaveEvent: (GameEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onSetUserBanned: (String, Boolean) -> Unit
) {
    BackHandler { onBack() }

    SkyCloudsBackground {
        if (!state.isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🚫 접근 권한이 없어요", style = MaterialTheme.typography.headlineMedium, color = CoralBerry)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "관리자 계정으로 로그인해야 볼 수 있는 화면이에요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftSlateText
                )
                Spacer(modifier = Modifier.height(16.dp))
                ChunkyActionButton(text = "돌아가기", onClick = onBack)
            }
            return@SkyCloudsBackground
        }

        var currentTab by remember { mutableStateOf(AdminTab.STATS) }
        var editingTreasure by remember { mutableStateOf<TreasureDefinition?>(null) }
        var showCreateTreasure by remember { mutableStateOf(false) }
        var editingItem by remember { mutableStateOf<ItemDefinition?>(null) }
        var showCreateItem by remember { mutableStateOf(false) }
        var editingLevel by remember { mutableStateOf<LevelConfig?>(null) }
        var editingRarity by remember { mutableStateOf<RarityConfig?>(null) }
        var showCreateEvent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로가기", tint = DeepOceanNavy)
                }
                Text(
                    text = "👑 관리자 대시보드",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepOceanNavy
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdminTab.entries) { tab ->
                    val selected = tab == currentTab
                    Surface(
                        color = if (selected) MagicLavender else Color.White,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { currentTab = tab }
                    ) {
                        Text(
                            text = "${tab.emoji} ${tab.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else DeepOceanNavy,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (currentTab) {
                AdminTab.STATS -> AdminStatsTab(
                    state = state,
                    users = adminUsers,
                    onSeedDefaults = onSeedDefaults
                )
                AdminTab.LEVELS -> AdminLevelsTab(
                    state = state,
                    onEditLevel = { editingLevel = it },
                    onEditRarity = { editingRarity = it }
                )
                AdminTab.TREASURES -> AdminTreasuresTab(
                    treasures = state.treasures,
                    onAddClick = { showCreateTreasure = true },
                    onEditClick = { editingTreasure = it },
                    onToggleActive = { t -> onSaveTreasure(t.copy(active = !t.active), false) },
                    onDeleteClick = { onDeleteTreasure(it.id) }
                )
                AdminTab.ITEMS -> AdminItemsTab(
                    items = state.items,
                    onAddClick = { showCreateItem = true },
                    onEditClick = { editingItem = it },
                    onToggleActive = { item -> onSaveItem(item.copy(active = !item.active), false) },
                    onDeleteClick = { onDeleteItem(it.id) }
                )
                AdminTab.NOTIFICATIONS -> AdminNotificationsTab(
                    state = state,
                    onSendNotification = onSendNotification
                )
                AdminTab.USERS -> AdminUsersTab(
                    users = adminUsers,
                    onToggleBan = onSetUserBanned
                )
                AdminTab.EVENTS -> AdminEventsTab(
                    events = state.events,
                    onAddEventClick = { showCreateEvent = true },
                    onToggleActive = { ev -> onSaveEvent(ev.copy(active = !ev.active)) },
                    onDeleteEvent = onDeleteEvent
                )
            }
        }

        if (showCreateTreasure || editingTreasure != null) {
            EditTreasureDialog(
                initial = editingTreasure,
                onSave = { treasure, notify ->
                    onSaveTreasure(treasure, notify)
                    showCreateTreasure = false
                    editingTreasure = null
                },
                onDismiss = {
                    showCreateTreasure = false
                    editingTreasure = null
                }
            )
        }

        if (showCreateItem || editingItem != null) {
            EditItemDialog(
                initial = editingItem,
                onSave = { item, notify ->
                    onSaveItem(item, notify)
                    showCreateItem = false
                    editingItem = null
                },
                onDismiss = {
                    showCreateItem = false
                    editingItem = null
                }
            )
        }

        editingLevel?.let { lvl ->
            EditLevelDialog(
                config = lvl,
                onSave = { updated ->
                    onSaveLevelConfig(updated)
                    editingLevel = null
                },
                onDismiss = { editingLevel = null }
            )
        }

        editingRarity?.let { rar ->
            EditRarityDialog(
                config = rar,
                onSave = { updated ->
                    onSaveRarityConfig(updated)
                    editingRarity = null
                },
                onDismiss = { editingRarity = null }
            )
        }

        if (showCreateEvent) {
            EditEventDialog(
                onSave = { ev ->
                    onSaveEvent(ev)
                    showCreateEvent = false
                },
                onDismiss = { showCreateEvent = false }
            )
        }
    }
}

@Composable
private fun AdminStatsTab(
    state: TreasureAppState,
    users: List<UserProfile>,
    onSeedDefaults: () -> Unit
) {
    val startOfDay = remember {
        val now = System.currentTimeMillis()
        now - (now % 86_400_000L)
    }
    val todayActiveUsers = users.count { (it.updatedAt?.seconds ?: 0L) * 1000L >= startOfDay }
    val totalCollected = users.sumOf { it.treasureCount }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("전체 회원", "${users.size}명", "👥", Modifier.weight(1f))
                StatCard("오늘 접속자", "${todayActiveUsers}명", "🌟", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("등록된 보물/아이템", "${state.treasures.size}/${state.items.size}", "💎", Modifier.weight(1f))
                StatCard("누적 획득 보물", "${totalCollected}개", "🧰", Modifier.weight(1f))
            }
        }
        item {
            Surface(color = SunnyGoldLight, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🌱 초기 데이터 시드", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                    Text(
                        "보물 20종, 아이템 6종, 레벨 1~10 설정, 등급 확률표를 Firestore에 한 번에 넣어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftSlateText
                    )
                    ChunkyActionButton(
                        text = "기본 데이터 넣기",
                        emoji = "📦",
                        containerColor = MintEmerald,
                        shadowColor = MintEmeraldDark,
                        contentColor = Color.White,
                        testTagName = "seed_default_data_button",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSeedDefaults
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = DeepOceanNavy)
        }
    }
}

@Composable
private fun AdminLevelsTab(
    state: TreasureAppState,
    onEditLevel: (LevelConfig) -> Unit,
    onEditRarity: (RarityConfig) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("🎲 레벨 구간별 보물 등급 출현 확률", style = MaterialTheme.typography.titleLarge, color = DeepOceanNavy)
        }
        items(state.rarityConfigs, key = { it.id }) { cfg ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditRarity(cfg) }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lv.${cfg.minLevel} ~ Lv.${cfg.maxLevel}", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text(
                            "일반 ${cfg.common} / 고급 ${cfg.uncommon} / 희귀 ${cfg.rare} / 영웅 ${cfg.hero} / 전설 ${cfg.legend}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftSlateText
                        )
                    }
                    Icon(Icons.Rounded.Edit, contentDescription = "확률 수정", tint = SkyBluePrimary)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("📈 레벨별 필요 경험치 및 보상", style = MaterialTheme.typography.titleLarge, color = DeepOceanNavy)
        }
        items(state.levelConfigs, key = { it.level }) { lvl ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditLevel(lvl) }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lv.${lvl.level}", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text(
                            "필요 EXP: ${lvl.needExp} · 보상 코인: ${lvl.rewardCoins}" +
                                if (lvl.rewardItemId.isNotBlank()) " · 선물: ${lvl.rewardItemId}" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftSlateText
                        )
                    }
                    Icon(Icons.Rounded.Edit, contentDescription = "레벨 수정", tint = SkyBluePrimary)
                }
            }
        }
    }
}

@Composable
private fun AdminTreasuresTab(
    treasures: List<TreasureDefinition>,
    onAddClick: () -> Unit,
    onEditClick: (TreasureDefinition) -> Unit,
    onToggleActive: (TreasureDefinition) -> Unit,
    onDeleteClick: (TreasureDefinition) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ChunkyActionButton(
                text = "새 보물 등록하기",
                emoji = "➕",
                containerColor = MintEmerald,
                shadowColor = MintEmeraldDark,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddClick
            )
        }
        items(treasures, key = { it.id }) { t ->
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = t.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                            Spacer(modifier = Modifier.width(6.dp))
                            RarityBadge(rarity = t.rarityEnum)
                        }
                        Text("Lv.${t.minLevel}+ · 🪙${t.coins} · ⭐${t.score}", style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                    }
                    Switch(checked = t.active, onCheckedChange = { onToggleActive(t) })
                    IconButton(onClick = { onEditClick(t) }) { Icon(Icons.Rounded.Edit, contentDescription = "수정", tint = SkyBluePrimary) }
                    IconButton(onClick = { onDeleteClick(t) }) { Icon(Icons.Rounded.Delete, contentDescription = "삭제", tint = CoralBerry) }
                }
            }
        }
    }
}

@Composable
private fun AdminItemsTab(
    items: List<ItemDefinition>,
    onAddClick: () -> Unit,
    onEditClick: (ItemDefinition) -> Unit,
    onToggleActive: (ItemDefinition) -> Unit,
    onDeleteClick: (ItemDefinition) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ChunkyActionButton(
                text = "새 아이템 등록하기",
                emoji = "➕",
                containerColor = MintEmerald,
                shadowColor = MintEmeraldDark,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddClick
            )
        }
        items(items, key = { it.id }) { item ->
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text("${item.effectEnum.labelKo} (${item.effectValue}) · 🪙${item.price} · Lv.${item.minLevel}+", style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                    }
                    Switch(checked = item.active, onCheckedChange = { onToggleActive(item) })
                    IconButton(onClick = { onEditClick(item) }) { Icon(Icons.Rounded.Edit, contentDescription = "수정", tint = SkyBluePrimary) }
                    IconButton(onClick = { onDeleteClick(item) }) { Icon(Icons.Rounded.Delete, contentDescription = "삭제", tint = CoralBerry) }
                }
            }
        }
    }
}

@Composable
private fun AdminNotificationsTab(
    state: TreasureAppState,
    onSendNotification: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var targetMinLevel by remember { mutableStateOf("1") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📣 탐험가 전체 알림 보내기", style = MaterialTheme.typography.titleLarge, color = DeepOceanNavy)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("알림 제목") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("알림 내용") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = targetMinLevel, onValueChange = { targetMinLevel = it }, label = { Text("대상 최소 레벨 (1 = 전체)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    ChunkyActionButton(
                        text = "알림 발송하기",
                        emoji = "📨",
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSendNotification(title, body, targetMinLevel.toLongOrNull() ?: 1L)
                            title = ""
                            body = ""
                        }
                    )
                }
            }
        }
        items(state.notifications, key = { it.id }) { n ->
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${n.title} (Lv.${n.targetMinLevel}+)", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                    Text(n.body, style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserProfile>,
    onToggleBan: (String, Boolean) -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users, key = { it.userId }) { u ->
            val joined = u.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "오늘"
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp)) {
                        VoxelCharacterAvatar(avatarKey = u.avatar)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.nickname, style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text("Lv.${u.level} · 가입일 $joined", style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                    }
                    TextButton(onClick = { onToggleBan(u.userId, !u.banned) }) {
                        Text(
                            text = if (u.banned) "해제하기" else "이용 정지",
                            color = if (u.banned) MintEmeraldDark else CoralBerry
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminEventsTab(
    events: List<GameEvent>,
    onAddEventClick: () -> Unit,
    onToggleActive: (GameEvent) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ChunkyActionButton(
                text = "기간 한정 이벤트 등록",
                emoji = "🎊",
                containerColor = MagicLavender,
                shadowColor = Color(0xFF6B2CB8),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddEventClick
            )
        }
        items(events, key = { it.id }) { ev ->
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${ev.title} (${ev.multiplierPercent / 100.0}배)", style = MaterialTheme.typography.titleMedium, color = DeepOceanNavy)
                        Text(ev.desc, style = MaterialTheme.typography.bodyMedium, color = SoftSlateText)
                        Text("종료: ${dateFmt.format(Date(ev.endAtMillis))}", style = MaterialTheme.typography.labelMedium, color = SkyBluePrimary)
                    }
                    Switch(checked = ev.active, onCheckedChange = { onToggleActive(ev) })
                    IconButton(onClick = { onDeleteEvent(ev.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "삭제", tint = CoralBerry)
                    }
                }
            }
        }
    }
}
