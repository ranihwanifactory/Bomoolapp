package com.example.ar

import com.example.model.DefaultGameCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArTreasureEngineTest {

    @Test
    fun spawnRoundTreasures_returnsFourToSevenEligibleTreasures() {
        val spawned = ArTreasureEngine.spawnRoundTreasures(
            playerLevel = 1L,
            allTreasures = DefaultGameCatalog.defaultTreasures,
            rarityConfigs = DefaultGameCatalog.defaultRarityConfigs,
            equippedItems = emptyList()
        )
        assertTrue("Expected 4..7 treasures, got ${spawned.size}", spawned.size in 4..7)
        assertTrue(spawned.all { it.definition.minLevel <= 1L })
    }

    @Test
    fun computeRoundDuration_withTimeItem_addsBonusSeconds() {
        val timeItem = DefaultGameCatalog.defaultItems.first { it.id == "item_time_15" }
        val base = ArTreasureEngine.computeRoundDurationSeconds(emptyList())
        val boosted = ArTreasureEngine.computeRoundDurationSeconds(listOf(timeItem))
        assertEquals(base + 15, boosted)
    }

    @Test
    fun projectTreasure_centeredTarget_isVisibleAndNearReticle() {
        val spawned = SpawnedTreasure(
            instanceId = "s1",
            definition = DefaultGameCatalog.defaultTreasures.first(),
            azimuthDeg = 180f,
            pitchDeg = 0f,
            distanceMeters = 2.5f,
            bobPhase = 0f
        )
        val projected = ArTreasureEngine.projectTreasure(
            spawned = spawned,
            cameraOrientation = DeviceOrientation(azimuthDeg = 180f, pitchDeg = 0f, hasHardwareSensor = true),
            magnifierScale = 1f
        )
        assertTrue(projected.isVisibleOnScreen)
        assertTrue(projected.isNearReticle)
        assertEquals(0.5f, projected.normX, 0.01f)
        assertEquals(0.5f, projected.normY, 0.01f)
    }
}
