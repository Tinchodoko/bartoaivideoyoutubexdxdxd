package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.BartoYouTubeTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.BartoScreen
import com.example.viewmodel.BartoViewModel

import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {

    private val viewModel: BartoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration with intent prior to UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            BartoYouTubeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BartoApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun BartoApp(viewModel: BartoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeChannel by viewModel.activeChannel.collectAsState()
    val availableChannels by viewModel.availableChannels.collectAsState()
    val connectedPlatforms by viewModel.connectedPlatforms.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val confirmDialog by viewModel.confirmDialog.collectAsState()

    var showChannelSwitcher by remember { mutableStateOf(false) }
    var showSyncDialogFromMain by remember { mutableStateOf(false) }

    val unreadNotifications = notifications.count { !it.isRead }
    val unreadComments = comments.count { !it.isAnswered }

    // Intercept back button for in-app navigation
    BackHandler(enabled = currentScreen != BartoScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            if (currentScreen != BartoScreen.PUBLISH_VIDEO &&
                currentScreen != BartoScreen.NOTIFICATIONS &&
                currentScreen != BartoScreen.SETTINGS
            ) {
                BartoTopBar(
                    channel = activeChannel,
                    unreadNotificationsCount = unreadNotifications,
                    onOpenChannelSwitcher = { showChannelSwitcher = true },
                    onOpenNotifications = { viewModel.navigateTo(BartoScreen.NOTIFICATIONS) },
                    onOpenAiStudio = { viewModel.navigateTo(BartoScreen.AI_STUDIO) },
                    onOpenSettings = { viewModel.navigateTo(BartoScreen.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (currentScreen != BartoScreen.PUBLISH_VIDEO &&
                currentScreen != BartoScreen.NOTIFICATIONS
            ) {
                BartoBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    unreadInboxCount = unreadComments
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                BartoScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                BartoScreen.VIDEOS -> VideosScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                BartoScreen.SHORTS -> ShortsScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                BartoScreen.INBOX -> InboxCommentsScreen(
                    viewModel = viewModel
                )
                BartoScreen.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel
                )
                BartoScreen.AI_STUDIO -> AiStudioScreen(
                    viewModel = viewModel
                )
                BartoScreen.PLAYLISTS -> PlaylistsScreen(
                    viewModel = viewModel
                )
                BartoScreen.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                BartoScreen.COMPETITORS -> CompetitorsScreen(
                    viewModel = viewModel
                )
                BartoScreen.SETTINGS -> ChannelSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateBack() }
                )
                BartoScreen.NOTIFICATIONS -> NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateBack() }
                )
                BartoScreen.PUBLISH_VIDEO -> PublishVideoScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateBack() }
                )
            }
        }
    }

    // Channel Switcher Modal
    if (showChannelSwitcher) {
        ChannelSwitcherModal(
            activeChannel = activeChannel,
            availableChannels = availableChannels,
            connectedPlatforms = connectedPlatforms,
            onSelectChannel = { channelId ->
                viewModel.switchChannel(channelId)
            },
            onOpenSync = {
                showSyncDialogFromMain = true
            },
            onDismiss = { showChannelSwitcher = false }
        )
    }

    // Direct YouTube Live Sync Modal
    if (showSyncDialogFromMain) {
        YouTubeSyncDialog(
            viewModel = viewModel,
            onDismiss = { showSyncDialogFromMain = false }
        )
    }

    // Confirmation Safety Dialog
    ConfirmationDialog(
        state = confirmDialog,
        onDismiss = { viewModel.dismissConfirmDialog() }
    )
}
