package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.DefaultGameCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class TreasureRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createInitialProfile_and_observeProfile_succeeds() = runBlocking {
        val uid = signInTestUser("alice_${UUID.randomUUID()}@test.com")
        val repo = TreasureRepository(firestore, auth)

        val createResult = withTimeout(TIMEOUT_MS) {
            repo.createInitialProfile("여우탐험가", "🦊")
        }
        assertTrue("Expected profile creation to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("여우탐험가", profile?.nickname)
        assertEquals(1L, profile?.level)
        assertEquals(100L, profile?.coins)
    }

    @Test
    fun buyItem_and_recordRoundResult_updatesProfileAndCollection() = runBlocking {
        val uid = signInTestUser("player_${UUID.randomUUID()}@test.com")
        val repo = TreasureRepository(firestore, auth)

        withTimeout(TIMEOUT_MS) {
            repo.createInitialProfile("토끼모험가", "🐰").getOrThrow()
        }
        val initialProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }!!

        val item = DefaultGameCatalog.defaultItems.first() // price = 40
        val buyResult = withTimeout(TIMEOUT_MS) {
            repo.buyItem(item, initialProfile, 0L)
        }
        assertTrue("Expected buyItem to succeed: ${buyResult.exceptionOrNull()}", buyResult.isSuccess)

        val afterBuyProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null && it.coins == 60L }
        }!!

        val found = DefaultGameCatalog.defaultTreasures.take(3)
        val roundResult = withTimeout(TIMEOUT_MS) {
            repo.recordRoundResult(
                currentProfile = afterBuyProfile,
                foundTreasures = found,
                equippedItemIds = listOf(item.id),
                currentInventory = mapOf(item.id to 1L),
                existingCollection = emptyMap(),
                levelConfigs = DefaultGameCatalog.defaultLevelConfigs,
                eventMultiplierPercent = 100L
            )
        }
        assertTrue("Expected round result to succeed: ${roundResult.exceptionOrNull()}", roundResult.isSuccess)

        val collection = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCollection(uid).first { it.size == 3 }
        }
        assertEquals(3, collection.size)
    }

    @Test
    fun observeUserProfile_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = TreasureRepository(firestore, auth)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserProfile("some_uid").first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val fsErr = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got $caught", fsErr)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }

    @Test
    fun crossUserRead_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice2_${UUID.randomUUID()}@test.com")
        val aliceRepo = TreasureRepository(firestore, auth)
        withTimeout(TIMEOUT_MS) {
            aliceRepo.createInitialProfile("앨리스", "🐱").getOrThrow()
        }

        signInTestUser("bob2_${UUID.randomUUID()}@test.com")
        val bobRepo = TreasureRepository(firestore, auth)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeUserProfile(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val fsErr = caught?.unwrapFirestoreException()
        assertNotNull("Expected PERMISSION_DENIED on cross-user read, got $caught", fsErr)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }

    private companion object {
        const val TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
