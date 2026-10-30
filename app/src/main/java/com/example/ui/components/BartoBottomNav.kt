package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BartoScreen

@Composable
fun BartoBottomNav(
    currentScreen: BartoScreen,
    onNavigate: (BartoScreen) -> Unit,
    unreadInboxCount: Int = 0
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        // Dashboard
        NavigationBarItem(
            selected = currentScreen == BartoScreen.DASHBOARD,
            onClick = { onNavigate(BartoScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == BartoScreen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Panel",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Panel", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                indicatorColor = YouTubeRed.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        // Videos
        NavigationBarItem(
            selected = currentScreen == BartoScreen.VIDEOS,
            onClick = { onNavigate(BartoScreen.VIDEOS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == BartoScreen.VIDEOS) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                    contentDescription = "Videos",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Videos", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                indicatorColor = YouTubeRed.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_videos")
        )

        // Shorts
        NavigationBarItem(
            selected = currentScreen == BartoScreen.SHORTS,
            onClick = { onNavigate(BartoScreen.SHORTS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == BartoScreen.SHORTS) Icons.Filled.FlashOn else Icons.Outlined.FlashOn,
                    contentDescription = "Shorts",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Shorts", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                indicatorColor = YouTubeRed.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_shorts")
        )

        // Inbox
        NavigationBarItem(
            selected = currentScreen == BartoScreen.INBOX,
            onClick = { onNavigate(BartoScreen.INBOX) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadInboxCount > 0) {
                            Badge(containerColor = YouTubeRed) {
                                Text(unreadInboxCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == BartoScreen.INBOX) Icons.Filled.Forum else Icons.Outlined.Forum,
                        contentDescription = "Bandeja",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = { Text("Bandeja", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                indicatorColor = YouTubeRed.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_inbox")
        )

        // Analytics
        NavigationBarItem(
            selected = currentScreen == BartoScreen.ANALYTICS,
            onClick = { onNavigate(BartoScreen.ANALYTICS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == BartoScreen.ANALYTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Métricas",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Métricas", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                indicatorColor = YouTubeRed.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_analytics")
        )

        // Gemini AI Studio
        NavigationBarItem(
            selected = currentScreen == BartoScreen.AI_STUDIO,
            onClick = { onNavigate(BartoScreen.AI_STUDIO) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == BartoScreen.AI_STUDIO) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                    contentDescription = "Estudio IA",
                    modifier = Modifier.size(22.dp),
                    tint = GeminiPurple
                )
            },
            label = { Text("IA Studio", fontSize = 11.sp, color = GeminiPurple) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GeminiPurple,
                indicatorColor = GeminiPurple.copy(alpha = 0.18f)
            ),
            modifier = Modifier.testTag("nav_ai_studio")
        )
    }
}
