package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TreasureDefinition
import com.example.model.TreasureRarity
import com.example.ui.components.ChunkyActionButton
import com.example.ui.components.RarityBadge
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldLight
import com.example.viewmodel.TreasureAppState

@Composable
fun CollectionScreen(
    state: TreasureAppState,
    onClaimCompletionBonus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTreasures = state.treasures.filter { it.active }
    val discoveredIds = state.collectionMap.keys
    var selectedRarity by remember { mutableStateOf<TreasureRarity?>(null) }
    var inspectedTreasure by remember { mutableStateOf<TreasureDefinition?>(null) }

    val filtered = remember(activeTreasures, selectedRarity) {
        if (selectedRarity == null) activeTreasures
        else activeTreasures.filter { it.rarityEnum == selectedRarity }
    }

    val totalCount = activeTreasures.size.coerceAtLeast(1)
    val foundUniqueCount = activeTreasures.count { it.id in discoveredIds }
    val progress = foundUniqueCount.toFloat() / totalCount.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = "🧰 내 보물상자 도감",
            style = MaterialTheme.typography.headlineMedium,
            color = DeepOceanNavy
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Progress summary card
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "수집 진행률",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanNavy
                    )
                    Text(
                        text = "$foundUniqueCount / ${activeTreasures.size} (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.titleMedium,
                        color = SkyBluePrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F1FC))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(SunnyGold)
                    )
                }
                if (foundUniqueCount >= activeTreasures.size && activeTreasures.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ChunkyActionButton(
                        text = "도감 완성 특별 보너스 받기! (+500코인)",
                        emoji = "👑",
                        containerColor = MintEmerald,
                        shadowColor = MintEmeraldDark,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onClaimCompletionBonus
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rarity filter chips with per-rarity counts
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val selected = selectedRarity == null
                Surface(
                    color = if (selected) SkyBluePrimary else Color.White,
                    shape = RoundedCornerShape(50),
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { selectedRarity = null }
                ) {
                    Text(
                        text = "전체 ($foundUniqueCount/${activeTreasures.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else DeepOceanNavy,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            items(TreasureRarity.entries) { rarity ->
                val totalInRarity = activeTreasures.count { it.rarityEnum == rarity }
                val foundInRarity = activeTreasures.count { it.rarityEnum == rarity && it.id in discoveredIds }
                val selected = selectedRarity == rarity
                Surface(
                    color = if (selected) rarity.badgeColor else rarity.bgColor,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { selectedRarity = rarity }
                ) {
                    Text(
                        text = "${rarity.labelKo} ($foundInRarity/$totalInRarity)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else rarity.badgeColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of treasures
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { treasure ->
                val entry = state.collectionMap[treasure.id]
                val discovered = entry != null
                Surface(
                    color = if (discovered) Color.White else Color(0xFFE9F0F5),
                    shape = RoundedCornerShape(20.dp),
                    border = if (discovered) BorderStroke(2.dp, treasure.rarityEnum.bgColor) else null,
                    shadowElevation = if (discovered) 3.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (discovered) inspectedTreasure = treasure }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RarityBadge(rarity = treasure.rarityEnum)
                            if (discovered) {
                                Text(
                                    text = "x${entry!!.count}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SkyBluePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(if (discovered) treasure.rarityEnum.bgColor else Color(0xFFD0DCE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (discovered) treasure.emoji else "❓",
                                fontSize = 34.sp,
                                modifier = Modifier.alpha(if (discovered) 1f else 0.65f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (discovered) treasure.name else "???",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = if (discovered) DeepOceanNavy else SoftSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (discovered) "🪙 ${treasure.coins}  ⭐ ${treasure.score}" else "Lv.${treasure.minLevel}+ 출현",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftSlateText
                        )
                    }
                }
            }
        }
    }

    inspectedTreasure?.let { t ->
        AlertDialog(
            onDismissRequest = { inspectedTreasure = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = t.emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = t.name, style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RarityBadge(rarity = t.rarityEnum)
                    Text(text = t.desc, style = MaterialTheme.typography.bodyLarge)
                    Surface(color = SunnyGoldLight, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = "획득 보상: 🪙 ${t.coins}코인 · 🏆 ${t.score}점",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedTreasure = null }) {
                    Text("닫기")
                }
            }
        )
    }
}
