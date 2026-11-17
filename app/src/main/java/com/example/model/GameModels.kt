package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RarityCommonBg
import com.example.ui.theme.RarityCommonColor
import com.example.ui.theme.RarityHeroBg
import com.example.ui.theme.RarityHeroColor
import com.example.ui.theme.RarityLegendBg
import com.example.ui.theme.RarityLegendColor
import com.example.ui.theme.RarityRareBg
import com.example.ui.theme.RarityRareColor
import com.example.ui.theme.RarityUncommonBg
import com.example.ui.theme.RarityUncommonColor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val ADMIN_EMAIL = "acehwan69@gmail.com"
const val MAX_LEVEL = 50L
const val INITIAL_STARTER_COINS = 100L

enum class TreasureRarity(
    val key: String,
    val labelKo: String,
    val badgeColor: Color,
    val bgColor: Color,
    val sortOrder: Int
) {
    COMMON("common", "일반", RarityCommonColor, RarityCommonBg, 0),
    UNCOMMON("uncommon", "고급", RarityUncommonColor, RarityUncommonBg, 1),
    RARE("rare", "희귀", RarityRareColor, RarityRareBg, 2),
    HERO("hero", "영웅", RarityHeroColor, RarityHeroBg, 3),
    LEGEND("legend", "전설", RarityLegendColor, RarityLegendBg, 4);

    companion object {
        fun fromKey(key: String): TreasureRarity =
            entries.firstOrNull { it.key == key } ?: COMMON
    }
}

enum class ItemEffectType(val key: String, val labelKo: String) {
    MAGNIFIER("magnifier", "보물 크기 확대"),
    RADAR("radar", "레이더 범위 강화"),
    TIME("time", "탐험 시간 연장"),
    LUCK("luck", "희귀 등급 확률 증가");

    companion object {
        fun fromKey(key: String): ItemEffectType =
            entries.firstOrNull { it.key == key } ?: MAGNIFIER
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val avatar: String = "vx_explorer",
    val level: Long = 1L,
    val exp: Long = 0L,
    val coins: Long = INITIAL_STARTER_COINS,
    val score: Long = 0L,
    val weeklyScore: Long = 0L,
    val treasureCount: Long = 0L,
    val banned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserInventoryItem(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val count: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserCollectionEntry(
    @DocumentId val id: String = "",
    val treasureId: String = "",
    val count: Long = 0L,
    @ServerTimestamp val firstAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ReadNotificationMarker(
    @DocumentId val id: String = "",
    val notificationId: String = "",
    @ServerTimestamp val readAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val avatar: String = "vx_explorer",
    val level: Long = 1L,
    val score: Long = 0L,
    val weeklyScore: Long = 0L,
    val treasureCount: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TreasureDefinition(
    @DocumentId val id: String = "",
    val name: String = "",
    val desc: String = "",
    val rarity: String = "common",
    val minLevel: Long = 1L,
    val score: Long = 10L,
    val coins: Long = 5L,
    val emoji: String = "🪙",
    val active: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val rarityEnum: TreasureRarity get() = TreasureRarity.fromKey(rarity)
}

@IgnoreExtraProperties
data class ItemDefinition(
    @DocumentId val id: String = "",
    val name: String = "",
    val desc: String = "",
    val effectType: String = "magnifier",
    val effectValue: Long = 30L,
    val price: Long = 50L,
    val minLevel: Long = 1L,
    val emoji: String = "🔍",
    val active: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectEnum: ItemEffectType get() = ItemEffectType.fromKey(effectType)
}

@IgnoreExtraProperties
data class LevelConfig(
    @DocumentId val id: String = "",
    val level: Long = 1L,
    val needExp: Long = 100L,
    val rewardCoins: Long = 30L,
    val rewardItemId: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RarityConfig(
    @DocumentId val id: String = "",
    val minLevel: Long = 1L,
    val maxLevel: Long = 10L,
    val common: Long = 60L,
    val uncommon: Long = 25L,
    val rare: Long = 10L,
    val hero: Long = 4L,
    val legend: Long = 1L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val targetMinLevel: Long = 1L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GameEvent(
    @DocumentId val id: String = "",
    val title: String = "",
    val desc: String = "",
    val multiplierPercent: Long = 200L,
    val startAtMillis: Long = 0L,
    val endAtMillis: Long = 0L,
    val active: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AvatarOption(val id: String, val badgeEmoji: String, val name: String)

object DefaultGameCatalog {
    val avatars = listOf(
        AvatarOption("vx_explorer", "🧭", "큐브 탐험가"),
        AvatarOption("vx_obby", "🎧", "오비 점퍼"),
        AvatarOption("vx_knight", "💎", "다이아 기사"),
        AvatarOption("vx_slime", "🟢", "큐브 슬라임"),
        AvatarOption("vx_cat", "🐱", "네모 냥이"),
        AvatarOption("vx_robot", "🤖", "픽셀 로봇"),
        AvatarOption("vx_wizard", "🧙", "별빛 마법사"),
        AvatarOption("vx_pirate", "🏴‍☠️", "블록 해적왕")
    )

    // 20 default treasures across all 5 rarities
    val defaultTreasures = listOf(
        TreasureDefinition(id = "t_branch", name = "마법 나뭇가지", desc = "숲속 요정이 남긴 반짝이는 나뭇가지예요.", rarity = "common", minLevel = 1, score = 15, coins = 8, emoji = "🪵", active = true),
        TreasureDefinition(id = "t_pebble", name = "반질반질 조약돌", desc = "강가에서 반짝반짝 빛나는 동그란 조약돌이에요.", rarity = "common", minLevel = 1, score = 18, coins = 10, emoji = "🪨", active = true),
        TreasureDefinition(id = "t_old_coin", name = "낡은 동전", desc = "오래된 모험가가 떨어뜨린 추억의 동전이에요.", rarity = "common", minLevel = 1, score = 20, coins = 12, emoji = "🪙", active = true),
        TreasureDefinition(id = "t_pinecone", name = "황금빛 솔방울", desc = "다람쥐가 아끼던 고소한 향기의 솔방울이에요.", rarity = "common", minLevel = 1, score = 22, coins = 12, emoji = "🌰", active = true),

        TreasureDefinition(id = "t_silver", name = "반짝 은화", desc = "달빛처럼 은은하게 빛나는 깨끗한 은화예요.", rarity = "uncommon", minLevel = 1, score = 40, coins = 22, emoji = "🥈", active = true),
        TreasureDefinition(id = "t_pearl", name = "조개 구슬", desc = "바다 요정이 숨겨 둔 동그란 진주 구슬이에요.", rarity = "uncommon", minLevel = 1, score = 45, coins = 25, emoji = "🫧", active = true),
        TreasureDefinition(id = "t_small_key", name = "작은 황동 열쇠", desc = "비밀 일기장을 열 수 있는 작고 귀여운 열쇠예요.", rarity = "uncommon", minLevel = 2, score = 50, coins = 28, emoji = "🗝️", active = true),
        TreasureDefinition(id = "t_clover", name = "빛나는 네잎클로버", desc = "찾으면 오늘 하루가 행운으로 가득해요!", rarity = "uncommon", minLevel = 2, score = 55, coins = 30, emoji = "🍀", active = true),

        TreasureDefinition(id = "t_gold_coin", name = "번쩍 금화", desc = "진짜 황금으로 만든 아주 귀한 동전이에요.", rarity = "rare", minLevel = 1, score = 90, coins = 48, emoji = "🏅", active = true),
        TreasureDefinition(id = "t_map_piece", name = "보물 지도 조각", desc = "전설의 섬을 가리키는 비밀 지도의 한 조각이에요.", rarity = "rare", minLevel = 2, score = 100, coins = 55, emoji = "🗺️", active = true),
        TreasureDefinition(id = "t_compass", name = "마법 나침반", desc = "언제나 진짜 보물이 있는 방향을 가리켜요.", rarity = "rare", minLevel = 3, score = 110, coins = 60, emoji = "🧭", active = true),
        TreasureDefinition(id = "t_hourglass", name = "별빛 모래시계", desc = "반짝이는 별가루가 천천히 떨어지는 시계예요.", rarity = "rare", minLevel = 4, score = 120, coins = 65, emoji = "⏳", active = true),

        TreasureDefinition(id = "t_fairy_crown", name = "요정의 왕관", desc = "봄꽃과 보석으로 만든 아기자기한 왕관이에요.", rarity = "hero", minLevel = 3, score = 220, coins = 120, emoji = "👑", active = true),
        TreasureDefinition(id = "t_dragon_scale", name = "용의 무지개 비늘", desc = "아기 용이 선물해 준 반짝이는 비늘이에요.", rarity = "hero", minLevel = 5, score = 250, coins = 135, emoji = "🐉", active = true),
        TreasureDefinition(id = "t_phoenix_feather", name = "불사조 깃털", desc = "따뜻한 햇살처럼 포근하게 빛나는 깃털이에요.", rarity = "hero", minLevel = 7, score = 270, coins = 150, emoji = "🪶", active = true),
        TreasureDefinition(id = "t_crystal_orb", name = "은하수 수정구", desc = "작은 별들이 빙글빙글 도는 신비로운 구슬이에요.", rarity = "hero", minLevel = 8, score = 290, coins = 160, emoji = "🔮", active = true),

        TreasureDefinition(id = "t_pirate_chest", name = "해적왕의 보물상자", desc = "전설 속 해적왕이 아끼던 황금 보물상자예요!", rarity = "legend", minLevel = 5, score = 500, coins = 280, emoji = "🧰", active = true),
        TreasureDefinition(id = "t_rainbow_diamond", name = "무지개 다이아몬드", desc = "일곱 빛깔로 눈부시게 빛나는 최고의 보석이에요!", rarity = "legend", minLevel = 7, score = 550, coins = 320, emoji = "💎", active = true),
        TreasureDefinition(id = "t_star_fragment", name = "천년의 별조각", desc = "하늘에서 떨어진 영롱한 소원의 별조각이에요.", rarity = "legend", minLevel = 10, score = 600, coins = 350, emoji = "🌟", active = true),
        TreasureDefinition(id = "t_sun_trophy", name = "태양신 황금 트로피", desc = "최고의 보물 탐험가만이 만날 수 있는 전설이에요!", rarity = "legend", minLevel = 12, score = 700, coins = 400, emoji = "🏆", active = true)
    )

    // 6 default explorer items
    val defaultItems = listOf(
        ItemDefinition(id = "item_magnifier_s", name = "꼬마 확대경", desc = "보물이 35% 더 크게 보여서 잡기 쉬워져요!", effectType = "magnifier", effectValue = 35, price = 40, minLevel = 1, emoji = "🔍", active = true),
        ItemDefinition(id = "item_radar_boost", name = "반짝 레이더 강화", desc = "레이더 힌트 범위와 방향 감지가 넓어져요!", effectType = "radar", effectValue = 50, price = 45, minLevel = 1, emoji = "📡", active = true),
        ItemDefinition(id = "item_time_15", name = "모래시계 (+15초)", desc = "보물찾기 제한시간을 15초 늘려줘요!", effectType = "time", effectValue = 15, price = 50, minLevel = 1, emoji = "⏱️", active = true),
        ItemDefinition(id = "item_luck_charm", name = "행운의 네잎 부적", desc = "희귀·영웅·전설 보물이 나올 확률이 올라가요!", effectType = "luck", effectValue = 35, price = 70, minLevel = 1, emoji = "🍀", active = true),
        ItemDefinition(id = "item_magnifier_l", name = "슈퍼 황금 돋보기", desc = "보물이 60% 커지고 조준도 빨라져요!", effectType = "magnifier", effectValue = 60, price = 90, minLevel = 3, emoji = "🔎", active = true),
        ItemDefinition(id = "item_time_30", name = "황금 모래시계 (+30초)", desc = "탐험 시간을 30초나 더 늘려줘요!", effectType = "time", effectValue = 30, price = 110, minLevel = 4, emoji = "⌛", active = true)
    )

    val defaultLevelConfigs: List<LevelConfig> = (1..10).map { lvl ->
        val rewardItem = when (lvl) {
            2 -> "item_magnifier_s"
            3 -> "item_time_15"
            4 -> "item_radar_boost"
            5 -> "item_luck_charm"
            7 -> "item_magnifier_l"
            10 -> "item_time_30"
            else -> ""
        }
        LevelConfig(
            id = lvl.toString(),
            level = lvl.toLong(),
            needExp = lvl * 100L,
            rewardCoins = 30L + lvl * 10L,
            rewardItemId = rewardItem
        )
    }

    val defaultRarityConfigs = listOf(
        RarityConfig(id = "1_5", minLevel = 1, maxLevel = 5, common = 55, uncommon = 28, rare = 12, hero = 4, legend = 1),
        RarityConfig(id = "6_15", minLevel = 6, maxLevel = 15, common = 38, uncommon = 30, rare = 20, hero = 9, legend = 3),
        RarityConfig(id = "16_30", minLevel = 16, maxLevel = 30, common = 25, uncommon = 28, rare = 25, hero = 15, legend = 7),
        RarityConfig(id = "31_50", minLevel = 31, maxLevel = 50, common = 15, uncommon = 22, rare = 28, hero = 22, legend = 13)
    )

    fun needExpForLevel(level: Long, configs: List<LevelConfig>): Long {
        return configs.firstOrNull { it.level == level }?.needExp ?: (level * 100L)
    }
}
