package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.model.GameEvent
import com.example.model.ItemDefinition
import com.example.model.ItemEffectType
import com.example.model.LevelConfig
import com.example.model.RarityConfig
import com.example.model.TreasureDefinition
import com.example.model.TreasureRarity
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.SkyBluePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTreasureDialog(
    initial: TreasureDefinition?,
    onSave: (TreasureDefinition, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var desc by remember { mutableStateOf(initial?.desc ?: "") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "💎") }
    var rarity by remember { mutableStateOf(initial?.rarity ?: "common") }
    var minLevel by remember { mutableStateOf((initial?.minLevel ?: 1L).toString()) }
    var score by remember { mutableStateOf((initial?.score ?: 50L).toString()) }
    var coins by remember { mutableStateOf((initial?.coins ?: 25L).toString()) }
    var notifyUsers by remember { mutableStateOf(initial == null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "새 보물 추가" else "보물 수정") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("보물 이름") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("설명") })
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("이모지") }, singleLine = true)
                Text("등급 선택", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TreasureRarity.entries.forEach { r ->
                        val selected = r.key == rarity
                        Surface(
                            color = if (selected) r.badgeColor else r.bgColor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { rarity = r.key }
                        ) {
                            Text(
                                text = r.labelKo,
                                color = if (selected) Color.White else r.badgeColor,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minLevel, onValueChange = { minLevel = it }, label = { Text("최소 Lv") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = score, onValueChange = { score = it }, label = { Text("점수") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = coins, onValueChange = { coins = it }, label = { Text("코인") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyUsers, onCheckedChange = { notifyUsers = it })
                    Text("회원들에게 알림 보내기", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        TreasureDefinition(
                            id = initial?.id ?: "",
                            name = name,
                            desc = desc.ifBlank { "새로 발견된 반짝이는 보물이에요!" },
                            rarity = rarity,
                            minLevel = minLevel.toLongOrNull() ?: 1L,
                            score = score.toLongOrNull() ?: 50L,
                            coins = coins.toLongOrNull() ?: 25L,
                            emoji = emoji.ifBlank { "💎" },
                            active = initial?.active ?: true
                        ),
                        notifyUsers
                    )
                }
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemDialog(
    initial: ItemDefinition?,
    onSave: (ItemDefinition, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var desc by remember { mutableStateOf(initial?.desc ?: "") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "🔍") }
    var effectType by remember { mutableStateOf(initial?.effectType ?: "magnifier") }
    var effectValue by remember { mutableStateOf((initial?.effectValue ?: 30L).toString()) }
    var price by remember { mutableStateOf((initial?.price ?: 50L).toString()) }
    var minLevel by remember { mutableStateOf((initial?.minLevel ?: 1L).toString()) }
    var notifyUsers by remember { mutableStateOf(initial == null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "새 아이템 추가" else "아이템 수정") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("아이템 이름") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("설명") })
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("이모지") }, singleLine = true)
                Text("효과 종류", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ItemEffectType.entries.forEach { eff ->
                        val selected = eff.key == effectType
                        Surface(
                            color = if (selected) SkyBluePrimary else Color(0xFFEAF5FD),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { effectType = eff.key }
                        ) {
                            Text(
                                text = eff.labelKo,
                                color = if (selected) Color.White else DeepOceanNavy,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = effectValue, onValueChange = { effectValue = it }, label = { Text("효과값") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("가격") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = minLevel, onValueChange = { minLevel = it }, label = { Text("최소Lv") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyUsers, onCheckedChange = { notifyUsers = it })
                    Text("회원들에게 알림 보내기", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        ItemDefinition(
                            id = initial?.id ?: "",
                            name = name,
                            desc = desc.ifBlank { "모험에 도움이 되는 아이템이에요." },
                            effectType = effectType,
                            effectValue = effectValue.toLongOrNull() ?: 30L,
                            price = price.toLongOrNull() ?: 50L,
                            minLevel = minLevel.toLongOrNull() ?: 1L,
                            emoji = emoji.ifBlank { "🔍" },
                            active = initial?.active ?: true
                        ),
                        notifyUsers
                    )
                }
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun EditLevelDialog(
    config: LevelConfig,
    onSave: (LevelConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var needExp by remember { mutableStateOf(config.needExp.toString()) }
    var rewardCoins by remember { mutableStateOf(config.rewardCoins.toString()) }
    var rewardItemId by remember { mutableStateOf(config.rewardItemId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lv.${config.level} 설정 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = needExp, onValueChange = { needExp = it }, label = { Text("필요 경험치") }, singleLine = true)
                OutlinedTextField(value = rewardCoins, onValueChange = { rewardCoins = it }, label = { Text("레벨업 보상 코인") }, singleLine = true)
                OutlinedTextField(value = rewardItemId, onValueChange = { rewardItemId = it }, label = { Text("보상 아이템 ID (선택)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    config.copy(
                        needExp = needExp.toLongOrNull() ?: config.needExp,
                        rewardCoins = rewardCoins.toLongOrNull() ?: config.rewardCoins,
                        rewardItemId = rewardItemId.trim()
                    )
                )
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun EditRarityDialog(
    config: RarityConfig,
    onSave: (RarityConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var common by remember { mutableStateOf(config.common.toString()) }
    var uncommon by remember { mutableStateOf(config.uncommon.toString()) }
    var rare by remember { mutableStateOf(config.rare.toString()) }
    var hero by remember { mutableStateOf(config.hero.toString()) }
    var legend by remember { mutableStateOf(config.legend.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lv.${config.minLevel}~${config.maxLevel} 출현 확률") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = common, onValueChange = { common = it }, label = { Text("일반 비중") }, singleLine = true)
                OutlinedTextField(value = uncommon, onValueChange = { uncommon = it }, label = { Text("고급 비중") }, singleLine = true)
                OutlinedTextField(value = rare, onValueChange = { rare = it }, label = { Text("희귀 비중") }, singleLine = true)
                OutlinedTextField(value = hero, onValueChange = { hero = it }, label = { Text("영웅 비중") }, singleLine = true)
                OutlinedTextField(value = legend, onValueChange = { legend = it }, label = { Text("전설 비중") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    config.copy(
                        common = common.toLongOrNull() ?: config.common,
                        uncommon = uncommon.toLongOrNull() ?: config.uncommon,
                        rare = rare.toLongOrNull() ?: config.rare,
                        hero = hero.toLongOrNull() ?: config.hero,
                        legend = legend.toLongOrNull() ?: config.legend
                    )
                )
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun EditEventDialog(
    onSave: (GameEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("주말 보물 2배 이벤트!") }
    var desc by remember { mutableStateOf("주말 동안 모든 보물 점수와 코인이 2배가 돼요!") }
    var multiplier by remember { mutableStateOf("200") }
    var durationHours by remember { mutableStateOf("48") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 기간 한정 이벤트 등록") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("이벤트 제목") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("설명") })
                OutlinedTextField(value = multiplier, onValueChange = { multiplier = it }, label = { Text("배율 (%) 예: 200 = 2배") }, singleLine = true)
                OutlinedTextField(value = durationHours, onValueChange = { durationHours = it }, label = { Text("지속 시간 (시간)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val now = System.currentTimeMillis()
                val hours = (durationHours.toLongOrNull() ?: 24L).coerceIn(1L, 720L)
                onSave(
                    GameEvent(
                        title = title.ifBlank { "보물 보너스 이벤트" },
                        desc = desc.ifBlank { "보너스 보상을 받아가세요!" },
                        multiplierPercent = (multiplier.toLongOrNull() ?: 200L).coerceIn(100L, 500L),
                        startAtMillis = now,
                        endAtMillis = now + hours * 3600L * 1000L,
                        active = true
                    )
                )
            }) { Text("등록") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
