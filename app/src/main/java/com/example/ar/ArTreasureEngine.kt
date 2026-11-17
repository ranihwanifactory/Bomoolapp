package com.example.ar

import com.example.model.DefaultGameCatalog
import com.example.model.ItemDefinition
import com.example.model.ItemEffectType
import com.example.model.RarityConfig
import com.example.model.TreasureDefinition
import com.example.model.TreasureRarity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class SpawnedTreasure(
    val instanceId: String,
    val definition: TreasureDefinition,
    val azimuthDeg: Float,   // 0..360
    val pitchDeg: Float,     // -26..26
    val distanceMeters: Float,
    val bobPhase: Float,
    val collected: Boolean = false
)

data class ProjectedTreasure(
    val treasure: SpawnedTreasure,
    val normX: Float,          // 0.5 is center, 0..1 visible
    val normY: Float,          // 0.5 is center, 0..1 visible
    val isVisibleOnScreen: Boolean,
    val isNearReticle: Boolean,
    val offscreenAngleDeg: Float,
    val visualScale: Float
)

object ArTreasureEngine {
    private const val HORIZONTAL_FOV_DEG = 64f
    private const val VERTICAL_FOV_DEG = 52f

    fun computeRoundDurationSeconds(equippedItems: List<ItemDefinition>): Int {
        val baseSeconds = 75
        val bonus = equippedItems
            .filter { it.effectEnum == ItemEffectType.TIME }
            .sumOf { it.effectValue.toInt() }
        return (baseSeconds + bonus).coerceIn(60, 150)
    }

    fun computeMagnifierScale(equippedItems: List<ItemDefinition>): Float {
        val boostPct = equippedItems
            .filter { it.effectEnum == ItemEffectType.MAGNIFIER }
            .sumOf { it.effectValue.toInt() }
        return 1f + (boostPct / 100f)
    }

    fun computeRadarRangeMeters(equippedItems: List<ItemDefinition>): Float {
        val boostPct = equippedItems
            .filter { it.effectEnum == ItemEffectType.RADAR }
            .sumOf { it.effectValue.toInt() }
        return 4.5f * (1f + boostPct / 100f)
    }

    fun spawnRoundTreasures(
        playerLevel: Long,
        allTreasures: List<TreasureDefinition>,
        rarityConfigs: List<RarityConfig>,
        equippedItems: List<ItemDefinition>
    ): List<SpawnedTreasure> {
        val eligible = allTreasures.filter { it.active && it.minLevel <= playerLevel }
            .ifEmpty { DefaultGameCatalog.defaultTreasures.filter { it.minLevel <= 1 } }

        val bracket = rarityConfigs.firstOrNull { playerLevel in it.minLevel..it.maxLevel }
            ?: rarityConfigs.lastOrNull()
            ?: DefaultGameCatalog.defaultRarityConfigs.first()

        val luckBoost = equippedItems
            .filter { it.effectEnum == ItemEffectType.LUCK }
            .sumOf { it.effectValue }

        val count = Random.nextInt(4, 8) // 4 to 7 hidden treasures per round
        val spawned = mutableListOf<SpawnedTreasure>()

        // Put at least one treasure relatively close to starting view so kids spot one quickly
        for (i in 0 until count) {
            val rarity = pickRarity(bracket, luckBoost)
            val candidates = eligible.filter { it.rarityEnum == rarity }
                .ifEmpty { eligible }
            val chosen = candidates.random()

            val azimuth = if (i == 0) {
                Random.nextFloat() * 44f - 22f // right ahead on start
            } else {
                Random.nextFloat() * 360f
            }
            val pitch = Random.nextFloat() * 38f - 19f
            val distance = Random.nextFloat() * 2.4f + 1.8f // 1.8m .. 4.2m

            spawned.add(
                SpawnedTreasure(
                    instanceId = "spawn_${i}_${System.nanoTime()}",
                    definition = chosen,
                    azimuthDeg = ArSensorController.normalizeAzimuth(azimuth),
                    pitchDeg = pitch,
                    distanceMeters = distance,
                    bobPhase = Random.nextFloat() * (2f * PI.toFloat())
                )
            )
        }
        return spawned
    }

    private fun pickRarity(bracket: RarityConfig, luckBoost: Long): TreasureRarity {
        val commonW = (bracket.common - luckBoost / 2).coerceAtLeast(5L)
        val uncommonW = bracket.uncommon
        val rareW = bracket.rare + luckBoost / 3
        val heroW = bracket.hero + luckBoost / 4
        val legendW = bracket.legend + luckBoost / 5

        val total = (commonW + uncommonW + rareW + heroW + legendW).coerceAtLeast(1L)
        var roll = Random.nextLong(total)
        if (roll < commonW) return TreasureRarity.COMMON
        roll -= commonW
        if (roll < uncommonW) return TreasureRarity.UNCOMMON
        roll -= uncommonW
        if (roll < rareW) return TreasureRarity.RARE
        roll -= rareW
        if (roll < heroW) return TreasureRarity.HERO
        return TreasureRarity.LEGEND
    }

    fun projectTreasure(
        spawned: SpawnedTreasure,
        cameraOrientation: DeviceOrientation,
        magnifierScale: Float
    ): ProjectedTreasure {
        val dAz = ArSensorController.shortestAngleDiff(cameraOrientation.azimuthDeg, spawned.azimuthDeg)
        val dPitch = spawned.pitchDeg - cameraOrientation.pitchDeg

        val normX = 0.5f + (dAz / HORIZONTAL_FOV_DEG)
        // Positive pitch is higher up -> smaller screen Y
        val normY = 0.5f - (dPitch / VERTICAL_FOV_DEG)

        val visible = normX in 0.08f..0.92f && normY in 0.12f..0.88f && abs(dAz) < 55f
        val distFromCenter = hypot(normX - 0.5f, normY - 0.5f)
        val nearReticle = visible && distFromCenter <= (0.11f * magnifierScale.coerceAtLeast(1f))

        val angleRad = atan2(-(dPitch).toDouble(), dAz.toDouble())
        val angleDeg = Math.toDegrees(angleRad).toFloat()

        val distScale = (3.0f / spawned.distanceMeters).coerceIn(0.65f, 1.35f)
        val scale = distScale * magnifierScale

        return ProjectedTreasure(
            treasure = spawned,
            normX = normX,
            normY = normY,
            isVisibleOnScreen = visible,
            isNearReticle = nearReticle,
            offscreenAngleDeg = angleDeg,
            visualScale = scale
        )
    }

    fun radarOffset(
        spawned: SpawnedTreasure,
        cameraOrientation: DeviceOrientation,
        maxRadarRange: Float
    ): Pair<Float, Float> {
        val relDeg = ArSensorController.shortestAngleDiff(cameraOrientation.azimuthDeg, spawned.azimuthDeg)
        val relRad = Math.toRadians(relDeg.toDouble())
        val normDist = (spawned.distanceMeters / maxRadarRange).coerceIn(0.15f, 0.92f)
        val x = (sin(relRad) * normDist).toFloat()
        val y = (-cos(relRad) * normDist).toFloat()
        return x to y
    }
}
