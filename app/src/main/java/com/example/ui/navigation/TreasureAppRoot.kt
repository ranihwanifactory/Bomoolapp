package com.example.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.audio.SoundEffectsManager
import com.example.data.PreferencesRepository
import com.example.data.TreasureRepository
import com.example.haptics.HapticsHelper
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OnboardingProfileScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.components.LevelUpCelebrationDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.SkyCloudsBackground
import com.example.ui.components.TutorialOverlayDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ArHuntScreen
import com.example.ui.screens.CollectionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.RankingScreen
import com.example.ui.theme.DeepOceanNavy
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSurface
import com.example.viewmodel.ProfileUiState
import com.example.viewmodel.TreasureViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_HOME = "home"
const val ROUTE_HUNT = "hunt"
const val ROUTE_COLLECTION = "collection"
const val ROUTE_RANKING = "ranking"
const val ROUTE_PROFILE = "profile"
const val ROUTE_ADMIN = "admin"

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val mainTabs = listOf(
    BottomNavTab(ROUTE_HOME, "홈", Icons.Rounded.Home, Icons.Outlined.Home),
    BottomNavTab(ROUTE_HUNT, "보물찾기", Icons.Rounded.Explore, Icons.Outlined.Explore),
    BottomNavTab(ROUTE_COLLECTION, "보물상자", Icons.Rounded.Inventory2, Icons.Outlined.Inventory2),
    BottomNavTab(ROUTE_RANKING, "랭킹", Icons.Rounded.Leaderboard, Icons.Outlined.Leaderboard),
    BottomNavTab(ROUTE_PROFILE, "내 정보", Icons.Rounded.Person, Icons.Outlined.Person)
)

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        androidx.compose.runtime.key(user.uid) {
            AuthenticatedTreasureApp(currentUserId = user.uid)
        }
    }
}

@Composable
private fun AuthenticatedTreasureApp(currentUserId: String) {
    val context = LocalContext.current
    val soundManager = remember { SoundEffectsManager() }
    val haptics = remember(context) { HapticsHelper(context.applicationContext) }

    val viewModel: TreasureViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                TreasureViewModel(
                    repository = TreasureRepository(db, Firebase.auth),
                    prefsRepository = PreferencesRepository(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()

    LaunchedEffect(state.localSettings) {
        soundManager.enabled = state.localSettings.soundEnabled
        haptics.enabled = state.localSettings.vibrationEnabled
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    when (val pState = state.profileState) {
        is ProfileUiState.Loading -> {
            SkyCloudsBackground {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SkyBluePrimary)
                        Text(
                            text = "탐험가 수첩을 펼치는 중...",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepOceanNavy,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
        is ProfileUiState.NeedsOnboarding -> {
            OnboardingProfileScreen(
                onSaveProfile = { nickname, avatar ->
                    soundManager.playPop()
                    haptics.tap()
                    viewModel.createProfile(nickname, avatar)
                }
            )
        }
        is ProfileUiState.Error -> {
            SkyCloudsBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pState.message, style = MaterialTheme.typography.bodyLarge, color = DeepOceanNavy)
                }
            }
        }
        is ProfileUiState.Ready -> {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

            var showNotificationsSheet by remember { mutableStateOf(false) }
            var showTutorialDialog by remember { mutableStateOf(!state.localSettings.tutorialSeen) }
            var levelUpCelebrationLevel by remember { mutableStateOf<Long?>(null) }
            var levelUpRewards by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(state.lastRoundSummary) {
                val summary = state.lastRoundSummary
                if (summary?.leveledUpFrom != null) {
                    levelUpCelebrationLevel = summary.newLevel
                    levelUpRewards = summary.rewardItemIds.mapNotNull { id ->
                        state.items.firstOrNull { it.id == id }?.let { "${it.emoji} ${it.name}" }
                    }
                    soundManager.playLevelUpFanfare()
                    haptics.levelUp()
                }
            }

            fun navigateTab(route: String) {
                soundManager.playPop()
                haptics.tap()
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            val showMainChrome = currentRoute != ROUTE_HUNT && currentRoute != ROUTE_ADMIN

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useSideRail = maxWidth >= 680.dp && showMainChrome

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showMainChrome && !useSideRail) {
                            NavigationBar(
                                containerColor = Color.White,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                mainTabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SkyBluePrimary,
                                            selectedTextColor = SkyBluePrimary,
                                            indicatorColor = SkyBlueSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useSideRail) {
                            NavigationRail(
                                containerColor = Color.White,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                            ) {
                                mainTabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_HOME,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(ROUTE_HOME) {
                                SkyCloudsBackground {
                                    HomeScreen(
                                        state = state,
                                        onStartHunt = { navigateTab(ROUTE_HUNT) },
                                        onToggleEquip = { id ->
                                            soundManager.playPop()
                                            haptics.tap()
                                            viewModel.toggleEquipItem(id)
                                        },
                                        onBuyItem = { item ->
                                            soundManager.playPop()
                                            haptics.tap()
                                            viewModel.buyItem(item)
                                        },
                                        onOpenNotifications = {
                                            soundManager.playPop()
                                            showNotificationsSheet = true
                                        },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                    )
                                }
                            }
                            composable(ROUTE_HUNT) {
                                ArHuntScreen(
                                    state = state,
                                    soundManager = soundManager,
                                    haptics = haptics,
                                    onRoundComplete = { found ->
                                        viewModel.finishRound(found)
                                    },
                                    onPlayAgain = {
                                        viewModel.dismissRoundSummary()
                                    },
                                    onExitToHome = {
                                        viewModel.dismissRoundSummary()
                                        navigateTab(ROUTE_HOME)
                                    }
                                )
                            }
                            composable(ROUTE_COLLECTION) {
                                SkyCloudsBackground {
                                    CollectionScreen(
                                        state = state,
                                        onClaimCompletionBonus = {
                                            soundManager.playCollectSparkle(true)
                                            haptics.levelUp()
                                            viewModel.claimCollectionBonus()
                                        },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                    )
                                }
                            }
                            composable(ROUTE_RANKING) {
                                SkyCloudsBackground {
                                    RankingScreen(
                                        state = state,
                                        currentUserId = currentUserId,
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                    )
                                }
                            }
                            composable(ROUTE_PROFILE) {
                                SkyCloudsBackground {
                                    ProfileSettingsScreen(
                                        state = state,
                                        onUpdateProfile = { nick, avatar ->
                                            soundManager.playPop()
                                            viewModel.updateProfile(nick, avatar)
                                        },
                                        onSetSound = { viewModel.setSoundEnabled(it) },
                                        onSetVibration = { viewModel.setVibrationEnabled(it) },
                                        onShowTutorial = { showTutorialDialog = true },
                                        onOpenAdminDashboard = {
                                            if (state.isAdmin) {
                                                soundManager.playPop()
                                                navController.navigate(ROUTE_ADMIN)
                                            }
                                        },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                    )
                                }
                            }
                            composable(ROUTE_ADMIN) {
                                AdminDashboardScreen(
                                    state = state,
                                    adminUsers = adminUsers,
                                    onBack = { navController.popBackStack() },
                                    onSeedDefaults = { viewModel.seedDefaultData() },
                                    onSaveTreasure = { t, n -> viewModel.saveTreasure(t, n) },
                                    onDeleteTreasure = { id -> viewModel.deleteTreasure(id) },
                                    onSaveItem = { i, n -> viewModel.saveItem(i, n) },
                                    onDeleteItem = { id -> viewModel.deleteItem(id) },
                                    onSaveLevelConfig = { cfg -> viewModel.saveLevelConfig(cfg) },
                                    onSaveRarityConfig = { cfg -> viewModel.saveRarityConfig(cfg) },
                                    onSendNotification = { title, body, lvl -> viewModel.sendNotification(title, body, lvl) },
                                    onSaveEvent = { ev -> viewModel.saveEvent(ev) },
                                    onDeleteEvent = { id -> viewModel.deleteEvent(id) },
                                    onSetUserBanned = { uid, banned -> viewModel.setUserBanned(uid, banned) }
                                )
                            }
                        }
                    }
                }
            }

            if (showTutorialDialog) {
                TutorialOverlayDialog(
                    onFinish = {
                        showTutorialDialog = false
                        viewModel.setTutorialSeen(true)
                    }
                )
            }

            if (showNotificationsSheet) {
                NotificationsSheet(
                    notifications = state.visibleNotifications,
                    readIds = state.readNotificationIds,
                    onMarkRead = { id -> viewModel.markNotificationRead(id) },
                    onDismiss = { showNotificationsSheet = false }
                )
            }

            levelUpCelebrationLevel?.let { lvl ->
                LevelUpCelebrationDialog(
                    newLevel = lvl,
                    rewardItemNames = levelUpRewards,
                    onDismiss = { levelUpCelebrationLevel = null }
                )
            }
        }
    }
}
