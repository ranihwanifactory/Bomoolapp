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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VoxelCharacterAvatar
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.SunnyGold
import com.example.ui.theme.SunnyGoldDark
import com.example.ui.theme.SunnyGoldLight
import com.example.viewmodel.TreasureAppState

private enum class RankingPeriod(val label: String) {
    WEEKLY("주간 랭킹"),
    ALL_TIME("전체 랭킹")
}

private enum class RankingSort(val label: String) {
    SCORE("점수순"),
    LEVEL("레벨순"),
    TREASURES("보물 수")
}

@Composable
fun RankingScreen(
    state: TreasureAppState,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(RankingPeriod.WEEKLY) }
    var sortBy by remember { mutableStateOf(RankingSort.SCORE) }

    val sortedEntries = remember(state.leaderboard, period, sortBy) {
        when (sortBy) {
            RankingSort.SCORE -> state.leaderboard.sortedByDescending {
                if (period == RankingPeriod.WEEKLY) it.weeklyScore else it.score
            }
            RankingSort.LEVEL -> state.leaderboard.sortedWith(
                compareByDescending<com.example.model.LeaderboardEntry> { it.level }
                    .thenByDescending { it.score }
            )
            RankingSort.TREASURES -> state.leaderboard.sortedByDescending { it.treasureCount }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = "🏆 탐험가 랭킹",
            style = MaterialTheme.typography.headlineMedium,
            color = DeepOceanNavy
        )
        Text(
            text = "별명과 캐릭터만 표시되어 안전해요!",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftSlateText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Period toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RankingPeriod.entries.forEach { p ->
                val selected = p == period
                Surface(
                    color = if (selected) SkyBluePrimary else Color.White,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { period = p }
                ) {
                    Text(
                        text = p.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Color.White else DeepOceanNavy,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sort criteria chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RankingSort.entries.forEach { s ->
                val selected = s == sortBy
                Surface(
                    color = if (selected) SunnyGoldLight else Color.White,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (selected) SunnyGoldDark else Color(0xFFD5EAF8)),
                    modifier = Modifier.clickable { sortBy = s }
                ) {
                    Text(
                        text = s.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepOceanNavy,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (sortedEntries.isEmpty()) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "첫 보물을 찾으면 랭킹에 올라가요!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftSlateText,
                    modifier = Modifier.padding(28.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(sortedEntries, key = { _, item -> item.userId }) { idx, entry ->
                    val rank = idx + 1
                    val isMe = entry.userId == currentUserId
                    val medal = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "${rank}위"
                    }
                    Surface(
                        color = if (isMe) SunnyGoldLight else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = if (isMe) BorderStroke(2.dp, SunnyGold) else null,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = medal,
                                    fontSize = if (rank <= 3) 24.sp else 16.sp,
                                    color = DeepOceanNavy
                                )
                            }
                            Surface(
                                color = SkyBlueSurface,
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    VoxelCharacterAvatar(
                                        avatarKey = entry.avatar,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        fallbackFontSize = 24.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.nickname + if (isMe) " (나)" else "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepOceanNavy
                                )
                                Text(
                                    text = "Lv.${entry.level} · 보물 ${entry.treasureCount}개",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftSlateText
                                )
                            }
                            val shownScore = if (period == RankingPeriod.WEEKLY) entry.weeklyScore else entry.score
                            Text(
                                text = "${shownScore}점",
                                style = MaterialTheme.typography.titleMedium,
                                color = SkyBluePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
