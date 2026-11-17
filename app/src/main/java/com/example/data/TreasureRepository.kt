package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ADMIN_EMAIL
import com.example.model.AppNotification
import com.example.model.DefaultGameCatalog
import com.example.model.GameEvent
import com.example.model.INITIAL_STARTER_COINS
import com.example.model.ItemDefinition
import com.example.model.LeaderboardEntry
import com.example.model.LevelConfig
import com.example.model.MAX_LEVEL
import com.example.model.RarityConfig
import com.example.model.ReadNotificationMarker
import com.example.model.TreasureDefinition
import com.example.model.UserCollectionEntry
import com.example.model.UserInventoryItem
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TreasureRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Google 로그인 후 이용할 수 있어요.")
    }

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(ADMIN_EMAIL, ignoreCase = true)
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(uid)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(logEx, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeInventory(uid: String): Flow<List<UserInventoryItem>> = flow {
        val path = "users/$uid/inventory"
        emitAll(
            db.collection("users").document(uid).collection("inventory")
                .snapshots()
                .map { snap -> snap.toObjects(UserInventoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCollection(uid: String): Flow<List<UserCollectionEntry>> = flow {
        val path = "users/$uid/collection"
        emitAll(
            db.collection("users").document(uid).collection("collection")
                .snapshots()
                .map { snap -> snap.toObjects(UserCollectionEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeReadNotifications(uid: String): Flow<Set<String>> = flow {
        val path = "users/$uid/readNotifications"
        emitAll(
            db.collection("users").document(uid).collection("readNotifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ReadNotificationMarker::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .map { it.notificationId.ifBlank { it.id } }
                        .toSet()
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val path = "leaderboard"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap -> snap.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTreasures(): Flow<List<TreasureDefinition>> = flow {
        val path = "treasures"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    val remote = snap.toObjects(TreasureDefinition::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (remote.isEmpty()) DefaultGameCatalog.defaultTreasures else remote
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeItems(): Flow<List<ItemDefinition>> = flow {
        val path = "items"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    val remote = snap.toObjects(ItemDefinition::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (remote.isEmpty()) DefaultGameCatalog.defaultItems else remote
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLevelConfigs(): Flow<List<LevelConfig>> = flow {
        val path = "levelConfigs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    val remote = snap.toObjects(LevelConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (remote.isEmpty()) DefaultGameCatalog.defaultLevelConfigs else remote.sortedBy { it.level }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeRarityConfigs(): Flow<List<RarityConfig>> = flow {
        val path = "rarityConfigs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    val remote = snap.toObjects(RarityConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (remote.isEmpty()) DefaultGameCatalog.defaultRarityConfigs else remote.sortedBy { it.minLevel }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeNotifications(): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeEvents(): Flow<List<GameEvent>> = flow {
        val path = "events"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap -> snap.toObjects(GameEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeAllUsersForAdmin(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap -> snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val logEx = fsErr ?: (error as? Exception)
                    if (logEx != null && auth.currentUser != null) {
                        handleFirestoreError(logEx, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createInitialProfile(nickname: String, avatar: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val cleanNickname = nickname.trim().take(20).ifEmpty { "보물탐험가" }
        val cleanAvatar = avatar.ifBlank { "vx_explorer" }
        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        val userPayload = mapOf(
            "userId" to uid,
            "nickname" to cleanNickname,
            "avatar" to cleanAvatar,
            "level" to 1L,
            "exp" to 0L,
            "coins" to INITIAL_STARTER_COINS,
            "score" to 0L,
            "weeklyScore" to 0L,
            "treasureCount" to 0L,
            "banned" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        batch.set(userRef, userPayload)

        val lbRef = db.collection("leaderboard").document(uid)
        val lbPayload = mapOf(
            "userId" to uid,
            "nickname" to cleanNickname,
            "avatar" to cleanAvatar,
            "level" to 1L,
            "score" to 0L,
            "weeklyScore" to 0L,
            "treasureCount" to 0L,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        batch.set(lbRef, lbPayload)

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userRef.path)
            throw e
        }
    }

    suspend fun updateNicknameAndAvatar(nickname: String, avatar: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val cleanNickname = nickname.trim().take(20).ifEmpty { "보물탐험가" }
        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        batch.update(
            userRef,
            mapOf(
                "nickname" to cleanNickname,
                "avatar" to avatar,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        val lbRef = db.collection("leaderboard").document(uid)
        batch.update(
            lbRef,
            mapOf(
                "nickname" to cleanNickname,
                "avatar" to avatar,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }

    suspend fun buyItem(
        item: ItemDefinition,
        currentProfile: UserProfile,
        currentOwnedCount: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(!currentProfile.banned) { "이용이 제한된 계정이에요." }
        require(currentProfile.level >= item.minLevel) { "레벨이 부족해요!" }
        require(currentProfile.coins >= item.price) { "코인이 부족해요!" }

        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        batch.update(
            userRef,
            mapOf(
                "coins" to (currentProfile.coins - item.price).coerceAtLeast(0L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        val invRef = userRef.collection("inventory").document(item.id)
        if (currentOwnedCount <= 0L) {
            batch.set(
                invRef,
                mapOf(
                    "itemId" to item.id,
                    "count" to 1L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        } else {
            batch.update(
                invRef,
                mapOf(
                    "count" to (currentOwnedCount + 1L).coerceAtMost(999L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, invRef.path)
            throw e
        }
    }

    data class RoundRewardComputation(
        val newLevel: Long,
        val newExp: Long,
        val newCoins: Long,
        val newScore: Long,
        val newWeeklyScore: Long,
        val newTreasureCount: Long,
        val leveledUpFrom: Long?,
        val rewardItemIds: List<String>
    )

    suspend fun recordRoundResult(
        currentProfile: UserProfile,
        foundTreasures: List<TreasureDefinition>,
        equippedItemIds: List<String>,
        currentInventory: Map<String, Long>,
        existingCollection: Map<String, UserCollectionEntry>,
        levelConfigs: List<LevelConfig>,
        eventMultiplierPercent: Long
    ): Result<RoundRewardComputation> = runCatching {
        val uid = requireUserId()
        require(!currentProfile.banned) { "이용이 제한된 계정이에요." }

        val multiplier = (eventMultiplierPercent.coerceIn(100L, 500L)) / 100.0
        val earnedScore = foundTreasures.sumOf { (it.score * multiplier).toLong() }.coerceAtMost(10000L)
        val earnedCoins = foundTreasures.sumOf { (it.coins * multiplier).toLong() }.coerceAtMost(4000L)
        val earnedExp = foundTreasures.sumOf { (it.score * 0.8 * multiplier).toLong().coerceAtLeast(10L) }

        var level = currentProfile.level
        var exp = currentProfile.exp + earnedExp
        var bonusCoins = 0L
        val rewardItemIds = mutableListOf<String>()

        while (level < MAX_LEVEL) {
            val need = DefaultGameCatalog.needExpForLevel(level, levelConfigs)
            if (exp >= need) {
                exp -= need
                level += 1L
                val nextConfig = levelConfigs.firstOrNull { it.level == level }
                bonusCoins += nextConfig?.rewardCoins ?: (30L + level * 10L)
                val rItem = nextConfig?.rewardItemId.orEmpty()
                if (rItem.isNotBlank()) {
                    rewardItemIds.add(rItem)
                }
            } else {
                break
            }
        }
        val cappedLevel = level.coerceAtMost(currentProfile.level + 5L).coerceAtMost(MAX_LEVEL)
        val totalAddedCoins = (earnedCoins + bonusCoins).coerceAtMost(5000L)
        val newCoins = currentProfile.coins + totalAddedCoins
        val newScore = currentProfile.score + earnedScore
        val newWeeklyScore = currentProfile.weeklyScore + earnedScore
        val newTreasureCount = currentProfile.treasureCount + foundTreasures.size.toLong().coerceAtMost(20L)

        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        batch.update(
            userRef,
            mapOf(
                "level" to cappedLevel,
                "exp" to exp,
                "coins" to newCoins,
                "score" to newScore,
                "weeklyScore" to newWeeklyScore,
                "treasureCount" to newTreasureCount,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        val lbRef = db.collection("leaderboard").document(uid)
        batch.set(
            lbRef,
            mapOf(
                "userId" to uid,
                "nickname" to currentProfile.nickname,
                "avatar" to currentProfile.avatar,
                "level" to cappedLevel,
                "score" to newScore,
                "weeklyScore" to newWeeklyScore,
                "treasureCount" to newTreasureCount,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        // Update collection counts grouped by treasureId
        val grouped = foundTreasures.groupingBy { it.id }.eachCount()
        for ((treasureId, added) in grouped) {
            if (treasureId.isBlank()) continue
            val colRef = userRef.collection("collection").document(treasureId)
            val prev = existingCollection[treasureId]
            if (prev == null) {
                batch.set(
                    colRef,
                    mapOf(
                        "treasureId" to treasureId,
                        "count" to added.toLong().coerceAtMost(10L),
                        "firstAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    colRef,
                    mapOf(
                        "count" to (prev.count + added.toLong().coerceAtMost(10L)),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }

        // Update inventory deltas (consumed equipped items + level-up reward items)
        val inventoryDelta = mutableMapOf<String, Long>()
        equippedItemIds.forEach { itemId ->
            inventoryDelta[itemId] = (inventoryDelta[itemId] ?: 0L) - 1L
        }
        rewardItemIds.forEach { itemId ->
            inventoryDelta[itemId] = (inventoryDelta[itemId] ?: 0L) + 1L
        }
        for ((itemId, delta) in inventoryDelta) {
            if (itemId.isBlank()) continue
            val prevCount = currentInventory[itemId] ?: 0L
            val nextCount = (prevCount + delta).coerceIn(0L, 999L)
            val invRef = userRef.collection("inventory").document(itemId)
            if (!currentInventory.containsKey(itemId)) {
                if (nextCount > 0L) {
                    batch.set(
                        invRef,
                        mapOf(
                            "itemId" to itemId,
                            "count" to nextCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            } else {
                batch.update(
                    invRef,
                    mapOf(
                        "count" to nextCount,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userRef.path)
            throw e
        }

        RoundRewardComputation(
            newLevel = cappedLevel,
            newExp = exp,
            newCoins = newCoins,
            newScore = newScore,
            newWeeklyScore = newWeeklyScore,
            newTreasureCount = newTreasureCount,
            leveledUpFrom = if (cappedLevel > currentProfile.level) currentProfile.level else null,
            rewardItemIds = rewardItemIds
        )
    }

    suspend fun claimCollectionCompletionBonus(currentProfile: UserProfile): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(!currentProfile.banned) { "이용이 제한된 계정이에요." }
        val userRef = db.collection("users").document(uid)
        try {
            userRef.update(
                mapOf(
                    "coins" to currentProfile.coins + 500L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("readNotifications").document(notificationId)
        try {
            ref.set(
                mapOf(
                    "notificationId" to notificationId,
                    "readAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    // --- Admin Actions ---

    suspend fun seedDefaultData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val batch = db.batch()
        DefaultGameCatalog.defaultTreasures.forEach { t ->
            val ref = db.collection("treasures").document(t.id)
            batch.set(
                ref,
                mapOf(
                    "name" to t.name,
                    "desc" to t.desc,
                    "rarity" to t.rarity,
                    "minLevel" to t.minLevel,
                    "score" to t.score,
                    "coins" to t.coins,
                    "emoji" to t.emoji,
                    "active" to t.active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        DefaultGameCatalog.defaultItems.forEach { item ->
            val ref = db.collection("items").document(item.id)
            batch.set(
                ref,
                mapOf(
                    "name" to item.name,
                    "desc" to item.desc,
                    "effectType" to item.effectType,
                    "effectValue" to item.effectValue,
                    "price" to item.price,
                    "minLevel" to item.minLevel,
                    "emoji" to item.emoji,
                    "active" to item.active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        DefaultGameCatalog.defaultLevelConfigs.forEach { lvl ->
            val ref = db.collection("levelConfigs").document(lvl.id)
            batch.set(
                ref,
                mapOf(
                    "level" to lvl.level,
                    "needExp" to lvl.needExp,
                    "rewardCoins" to lvl.rewardCoins,
                    "rewardItemId" to lvl.rewardItemId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        DefaultGameCatalog.defaultRarityConfigs.forEach { r ->
            val ref = db.collection("rarityConfigs").document(r.id)
            batch.set(
                ref,
                mapOf(
                    "minLevel" to r.minLevel,
                    "maxLevel" to r.maxLevel,
                    "common" to r.common,
                    "uncommon" to r.uncommon,
                    "rare" to r.rare,
                    "hero" to r.hero,
                    "legend" to r.legend,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed/$uid")
            throw e
        }
    }

    suspend fun saveTreasure(treasure: TreasureDefinition, notifyUsers: Boolean = false): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docId = treasure.id.ifBlank { "t_${System.currentTimeMillis()}" }
        val ref = db.collection("treasures").document(docId)
        val payload = mapOf(
            "name" to treasure.name.trim(),
            "desc" to treasure.desc.trim(),
            "rarity" to treasure.rarity,
            "minLevel" to treasure.minLevel.coerceIn(1L, MAX_LEVEL),
            "score" to treasure.score.coerceIn(1L, 100000L),
            "coins" to treasure.coins.coerceIn(1L, 50000L),
            "emoji" to treasure.emoji.ifBlank { "💎" },
            "active" to treasure.active,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            ref.set(payload).await()
            if (notifyUsers) {
                sendNotification(
                    title = "새로운 보물 등장! ${treasure.emoji}",
                    body = "${treasure.name} 보물이 추가되었어요! 지금 찾아보세요!",
                    targetMinLevel = treasure.minLevel
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun deleteTreasure(treasureId: String): Result<Unit> = runCatching {
        requireUserId()
        val ref = db.collection("treasures").document(treasureId)
        try {
            ref.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun saveItem(item: ItemDefinition, notifyUsers: Boolean = false): Result<Unit> = runCatching {
        requireUserId()
        val docId = item.id.ifBlank { "item_${System.currentTimeMillis()}" }
        val ref = db.collection("items").document(docId)
        val payload = mapOf(
            "name" to item.name.trim(),
            "desc" to item.desc.trim(),
            "effectType" to item.effectType,
            "effectValue" to item.effectValue.coerceIn(1L, 1000L),
            "price" to item.price.coerceIn(0L, 100000L),
            "minLevel" to item.minLevel.coerceIn(1L, MAX_LEVEL),
            "emoji" to item.emoji.ifBlank { "🔍" },
            "active" to item.active,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            ref.set(payload).await()
            if (notifyUsers) {
                sendNotification(
                    title = "새 아이템 입고! ${item.emoji}",
                    body = "상점에 '${item.name}' 아이템이 추가되었어요!",
                    targetMinLevel = item.minLevel
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        requireUserId()
        val ref = db.collection("items").document(itemId)
        try {
            ref.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun saveLevelConfig(config: LevelConfig): Result<Unit> = runCatching {
        requireUserId()
        val docId = config.id.ifBlank { config.level.toString() }
        val ref = db.collection("levelConfigs").document(docId)
        try {
            ref.set(
                mapOf(
                    "level" to config.level.coerceIn(1L, MAX_LEVEL),
                    "needExp" to config.needExp.coerceIn(10L, 100000L),
                    "rewardCoins" to config.rewardCoins.coerceIn(0L, 50000L),
                    "rewardItemId" to config.rewardItemId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun saveRarityConfig(config: RarityConfig): Result<Unit> = runCatching {
        requireUserId()
        val docId = config.id.ifBlank { "${config.minLevel}_${config.maxLevel}" }
        val ref = db.collection("rarityConfigs").document(docId)
        try {
            ref.set(
                mapOf(
                    "minLevel" to config.minLevel.coerceIn(1L, MAX_LEVEL),
                    "maxLevel" to config.maxLevel.coerceIn(config.minLevel, MAX_LEVEL),
                    "common" to config.common.coerceIn(0L, 100L),
                    "uncommon" to config.uncommon.coerceIn(0L, 100L),
                    "rare" to config.rare.coerceIn(0L, 100L),
                    "hero" to config.hero.coerceIn(0L, 100L),
                    "legend" to config.legend.coerceIn(0L, 100L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun sendNotification(title: String, body: String, targetMinLevel: Long): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docId = "noti_${System.currentTimeMillis()}"
        val ref = db.collection("notifications").document(docId)
        try {
            ref.set(
                mapOf(
                    "title" to title.trim().take(80),
                    "body" to body.trim().take(400),
                    "targetMinLevel" to targetMinLevel.coerceIn(1L, MAX_LEVEL),
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun saveEvent(event: GameEvent): Result<Unit> = runCatching {
        requireUserId()
        val docId = event.id.ifBlank { "evt_${System.currentTimeMillis()}" }
        val ref = db.collection("events").document(docId)
        try {
            ref.set(
                mapOf(
                    "title" to event.title.trim().take(80),
                    "desc" to event.desc.trim().take(200),
                    "multiplierPercent" to event.multiplierPercent.coerceIn(100L, 500L),
                    "startAtMillis" to event.startAtMillis,
                    "endAtMillis" to event.endAtMillis.coerceAtLeast(event.startAtMillis),
                    "active" to event.active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        requireUserId()
        val ref = db.collection("events").document(eventId)
        try {
            ref.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun setUserBanned(targetUid: String, banned: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val ref = db.collection("users").document(targetUid)
        try {
            ref.update(
                mapOf(
                    "banned" to banned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            throw e
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }
}
