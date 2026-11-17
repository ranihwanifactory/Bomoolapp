package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LocalSettings
import com.example.data.PreferencesRepository
import com.example.data.TreasureRepository
import com.example.model.AppNotification
import com.example.model.DefaultGameCatalog
import com.example.model.GameEvent
import com.example.model.ItemDefinition
import com.example.model.LeaderboardEntry
import com.example.model.LevelConfig
import com.example.model.RarityConfig
import com.example.model.TreasureDefinition
import com.example.model.UserCollectionEntry
import com.example.model.UserInventoryItem
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object NeedsOnboarding : ProfileUiState
    data class Ready(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

data class RoundSummary(
    val collectedTreasures: List<TreasureDefinition>,
    val earnedCoins: Long,
    val earnedExp: Long,
    val earnedScore: Long,
    val leveledUpFrom: Long?,
    val newLevel: Long,
    val rewardItemIds: List<String>
)

data class TreasureAppState(
    val profileState: ProfileUiState = ProfileUiState.Loading,
    val treasures: List<TreasureDefinition> = DefaultGameCatalog.defaultTreasures,
    val items: List<ItemDefinition> = DefaultGameCatalog.defaultItems,
    val levelConfigs: List<LevelConfig> = DefaultGameCatalog.defaultLevelConfigs,
    val rarityConfigs: List<RarityConfig> = DefaultGameCatalog.defaultRarityConfigs,
    val inventory: List<UserInventoryItem> = emptyList(),
    val collection: List<UserCollectionEntry> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val readNotificationIds: Set<String> = emptySet(),
    val events: List<GameEvent> = emptyList(),
    val localSettings: LocalSettings = LocalSettings(),
    val equippedItemIds: List<String> = emptyList(),
    val isAdmin: Boolean = false,
    val lastRoundSummary: RoundSummary? = null,
    val statusMessage: String? = null
) {
    val activeProfile: UserProfile?
        get() = (profileState as? ProfileUiState.Ready)?.profile

    val inventoryMap: Map<String, Long>
        get() = inventory.associate { it.itemId.ifBlank { it.id } to it.count }

    val collectionMap: Map<String, UserCollectionEntry>
        get() = collection.associateBy { it.treasureId.ifBlank { it.id } }

    val visibleNotifications: List<AppNotification>
        get() {
            val lvl = activeProfile?.level ?: 1L
            return notifications.filter { it.targetMinLevel <= lvl }
        }

    val unreadNotificationCount: Int
        get() = visibleNotifications.count { it.id !in readNotificationIds }

    val activeEvent: GameEvent?
        get() {
            val now = System.currentTimeMillis()
            return events.firstOrNull { it.active && (it.startAtMillis == 0L || now in it.startAtMillis..it.endAtMillis) }
        }

    val activeEventMultiplierPercent: Long
        get() = activeEvent?.multiplierPercent ?: 100L
}

private data class CatalogBundle(
    val treasures: List<TreasureDefinition>,
    val items: List<ItemDefinition>,
    val levelConfigs: List<LevelConfig>,
    val rarityConfigs: List<RarityConfig>,
    val events: List<GameEvent>
)

private data class UserDataBundle(
    val profileState: ProfileUiState,
    val inventory: List<UserInventoryItem>,
    val collection: List<UserCollectionEntry>,
    val readIds: Set<String>,
    val notifications: List<AppNotification>,
    val leaderboard: List<LeaderboardEntry>
)

class TreasureViewModel(
    private val repository: TreasureRepository,
    private val prefsRepository: PreferencesRepository,
    private val currentUserId: String
) : ViewModel() {

    private val equippedIdsFlow = MutableStateFlow<List<String>>(emptyList())
    private val roundSummaryFlow = MutableStateFlow<RoundSummary?>(null)
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val adminUsersFlow = MutableStateFlow<List<UserProfile>>(emptyList())

    val adminUsers: StateFlow<List<UserProfile>> = adminUsersFlow

    init {
        if (repository.isCurrentUserAdmin()) {
            viewModelScope.launch {
                repository.observeAllUsersForAdmin()
                    .catch { err ->
                        val fsErr = unwrapFirestoreError(err)
                        if (fsErr == null && err is CancellationException) throw err
                        Log.w(TAG, "Admin users stream error", fsErr ?: err)
                    }
                    .collect { list ->
                        adminUsersFlow.value = list.sortedByDescending { it.level }
                    }
            }
        }
    }

    private val profileUiFlow = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, ProfileUiState> { profile ->
            if (profile == null) ProfileUiState.NeedsOnboarding else ProfileUiState.Ready(profile)
        }
        .catch { err ->
            val fsErr = unwrapFirestoreError(err)
            if (fsErr == null && err is CancellationException) throw err
            val root = fsErr ?: err
            Log.w(TAG, "Profile stream error", root)
            emit(ProfileUiState.Error(root.message ?: "프로필을 불러오지 못했어요."))
        }

    private val catalogFlow = combine(
        repository.observeTreasures().catch { emit(DefaultGameCatalog.defaultTreasures) },
        repository.observeItems().catch { emit(DefaultGameCatalog.defaultItems) },
        repository.observeLevelConfigs().catch { emit(DefaultGameCatalog.defaultLevelConfigs) },
        repository.observeRarityConfigs().catch { emit(DefaultGameCatalog.defaultRarityConfigs) },
        repository.observeEvents().catch { emit(emptyList()) }
    ) { treasures, items, levels, rarities, events ->
        CatalogBundle(treasures, items, levels, rarities, events)
    }

    private val userDataFlow = combine(
        profileUiFlow,
        repository.observeInventory(currentUserId).catch { emit(emptyList()) },
        repository.observeCollection(currentUserId).catch { emit(emptyList()) },
        repository.observeReadNotifications(currentUserId).catch { emit(emptySet()) },
        repository.observeNotifications().catch { emit(emptyList()) }
    ) { profileState, inventory, collection, readIds, notifications ->
        UserDataBundle(profileState, inventory, collection, readIds, notifications, emptyList())
    }

    private val combinedUserAndLeaderboard = combine(
        userDataFlow,
        repository.observeLeaderboard().catch { emit(emptyList()) }
    ) { userData, leaderboard ->
        userData.copy(leaderboard = leaderboard)
    }

    val uiState: StateFlow<TreasureAppState> = combine(
        combinedUserAndLeaderboard,
        catalogFlow,
        prefsRepository.settingsFlow,
        equippedIdsFlow,
        roundSummaryFlow,
        statusMessageFlow
    ) { args ->
        val userData = args[0] as UserDataBundle
        val catalog = args[1] as CatalogBundle
        val localSettings = args[2] as LocalSettings
        @Suppress("UNCHECKED_CAST")
        val equipped = args[3] as List<String>
        val summary = args[4] as RoundSummary?
        val status = args[5] as String?

        TreasureAppState(
            profileState = userData.profileState,
            treasures = catalog.treasures,
            items = catalog.items,
            levelConfigs = catalog.levelConfigs,
            rarityConfigs = catalog.rarityConfigs,
            inventory = userData.inventory,
            collection = userData.collection,
            leaderboard = userData.leaderboard,
            notifications = userData.notifications,
            readNotificationIds = userData.readIds,
            events = catalog.events,
            localSettings = localSettings,
            equippedItemIds = equipped,
            isAdmin = repository.isCurrentUserAdmin(),
            lastRoundSummary = summary,
            statusMessage = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TreasureAppState(isAdmin = repository.isCurrentUserAdmin())
    )

    fun createProfile(nickname: String, avatar: String) {
        viewModelScope.launch {
            val res = repository.createInitialProfile(nickname, avatar)
            if (res.isFailure) {
                statusMessageFlow.value = res.exceptionOrNull()?.localizedMessage ?: "프로필 저장에 실패했어요."
            }
        }
    }

    fun updateProfile(nickname: String, avatar: String) {
        viewModelScope.launch {
            val res = repository.updateNicknameAndAvatar(nickname, avatar)
            statusMessageFlow.value = if (res.isSuccess) {
                "프로필이 멋지게 바뀌었어요!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "수정에 실패했어요."
            }
        }
    }

    fun toggleEquipItem(itemId: String) {
        val state = uiState.value
        val owned = state.inventoryMap[itemId] ?: 0L
        val current = equippedIdsFlow.value.toMutableList()
        if (current.contains(itemId)) {
            current.remove(itemId)
            equippedIdsFlow.value = current
        } else {
            if (owned <= 0L) {
                statusMessageFlow.value = "가진 아이템이 없어요! 먼저 상점에서 코인으로 구매해 보세요."
                return
            }
            if (current.size >= 2) {
                statusMessageFlow.value = "아이템은 한 판에 최대 2개까지만 장착할 수 있어요!"
                return
            }
            current.add(itemId)
            equippedIdsFlow.value = current
        }
    }

    fun buyItem(item: ItemDefinition) {
        val state = uiState.value
        val profile = state.activeProfile ?: return
        if (profile.banned) {
            statusMessageFlow.value = "이용이 제한된 계정이에요."
            return
        }
        val owned = state.inventoryMap[item.id] ?: 0L
        viewModelScope.launch {
            val res = repository.buyItem(item, profile, owned)
            statusMessageFlow.value = if (res.isSuccess) {
                "${item.emoji} ${item.name}을(를) 가방에 넣었어요!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "구매할 수 없어요."
            }
        }
    }

    fun finishRound(foundTreasures: List<TreasureDefinition>) {
        val state = uiState.value
        val profile = state.activeProfile ?: return
        val equipped = equippedIdsFlow.value
        val multiplierPct = state.activeEventMultiplierPercent
        val multiplier = (multiplierPct.coerceIn(100L, 500L)) / 100.0
        val earnedScore = foundTreasures.sumOf { (it.score * multiplier).toLong() }
        val earnedCoins = foundTreasures.sumOf { (it.coins * multiplier).toLong() }
        val earnedExp = foundTreasures.sumOf { (it.score * 0.8 * multiplier).toLong().coerceAtLeast(10L) }

        viewModelScope.launch {
            val res = repository.recordRoundResult(
                currentProfile = profile,
                foundTreasures = foundTreasures,
                equippedItemIds = equipped,
                currentInventory = state.inventoryMap,
                existingCollection = state.collectionMap,
                levelConfigs = state.levelConfigs,
                eventMultiplierPercent = multiplierPct
            )
            if (res.isSuccess) {
                val computed = res.getOrThrow()
                equippedIdsFlow.value = emptyList()
                roundSummaryFlow.value = RoundSummary(
                    collectedTreasures = foundTreasures,
                    earnedCoins = earnedCoins,
                    earnedExp = earnedExp,
                    earnedScore = earnedScore,
                    leveledUpFrom = computed.leveledUpFrom,
                    newLevel = computed.newLevel,
                    rewardItemIds = computed.rewardItemIds
                )
            } else {
                statusMessageFlow.value = res.exceptionOrNull()?.localizedMessage ?: "결과 저장에 실패했어요."
            }
        }
    }

    fun dismissRoundSummary() {
        roundSummaryFlow.value = null
    }

    fun claimCollectionBonus() {
        val profile = uiState.value.activeProfile ?: return
        viewModelScope.launch {
            val res = repository.claimCollectionCompletionBonus(profile)
            statusMessageFlow.value = if (res.isSuccess) {
                "도감 완성 축하 보너스 500 코인을 받았어요!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "보상을 받지 못했어요."
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setVibrationEnabled(enabled) }
    }

    fun setTutorialSeen(seen: Boolean) {
        viewModelScope.launch { prefsRepository.setTutorialSeen(seen) }
    }

    fun clearStatusMessage() {
        statusMessageFlow.value = null
    }

    // --- Admin Actions ---
    fun seedDefaultData() {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.seedDefaultData()
            statusMessageFlow.value = if (res.isSuccess) {
                "기본 보물 20종, 아이템 6종, 레벨 1~10 설정을 넣었어요!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "기본 데이터 저장 실패"
            }
        }
    }

    fun saveTreasure(treasure: TreasureDefinition, notifyUsers: Boolean) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.saveTreasure(treasure, notifyUsers)
            statusMessageFlow.value = if (res.isSuccess) "보물을 저장했어요!" else "보물 저장 실패"
        }
    }

    fun deleteTreasure(treasureId: String) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.deleteTreasure(treasureId)
            statusMessageFlow.value = if (res.isSuccess) "보물을 삭제했어요!" else "보물 삭제 실패"
        }
    }

    fun saveItem(item: ItemDefinition, notifyUsers: Boolean) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.saveItem(item, notifyUsers)
            statusMessageFlow.value = if (res.isSuccess) "아이템을 저장했어요!" else "아이템 저장 실패"
        }
    }

    fun deleteItem(itemId: String) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.deleteItem(itemId)
            statusMessageFlow.value = if (res.isSuccess) "아이템을 삭제했어요!" else "아이템 삭제 실패"
        }
    }

    fun saveLevelConfig(config: LevelConfig) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.saveLevelConfig(config)
            statusMessageFlow.value = if (res.isSuccess) "레벨 설정을 저장했어요!" else "레벨 설정 실패"
        }
    }

    fun saveRarityConfig(config: RarityConfig) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.saveRarityConfig(config)
            statusMessageFlow.value = if (res.isSuccess) "등급 확률을 저장했어요!" else "확률 저장 실패"
        }
    }

    fun sendNotification(title: String, body: String, targetMinLevel: Long) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.sendNotification(title, body, targetMinLevel)
            statusMessageFlow.value = if (res.isSuccess) "알림을 발송했어요!" else "알림 발송 실패"
        }
    }

    fun saveEvent(event: GameEvent) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.saveEvent(event)
            statusMessageFlow.value = if (res.isSuccess) "이벤트를 저장했어요!" else "이벤트 저장 실패"
        }
    }

    fun deleteEvent(eventId: String) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.deleteEvent(eventId)
            statusMessageFlow.value = if (res.isSuccess) "이벤트를 삭제했어요!" else "이벤트 삭제 실패"
        }
    }

    fun setUserBanned(targetUid: String, banned: Boolean) {
        if (!repository.isCurrentUserAdmin()) return
        viewModelScope.launch {
            val res = repository.setUserBanned(targetUid, banned)
            statusMessageFlow.value = if (res.isSuccess) {
                if (banned) "회원을 이용 정지했어요." else "이용 정지를 해제했어요."
            } else {
                "회원 상태 변경 실패"
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    private companion object {
        const val TAG = "TreasureVM"
    }
}
