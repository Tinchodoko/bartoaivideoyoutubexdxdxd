package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.BartoScreen
import com.example.viewmodel.BartoViewModel

@Composable
fun DashboardScreen(
    viewModel: BartoViewModel,
    onNavigate: (BartoScreen) -> Unit
) {
    val channel by viewModel.activeChannel.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val period by viewModel.analyticsPeriod.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val scheduledPosts by viewModel.scheduledPosts.collectAsState()

    val latestVideo = videos.firstOrNull { it.uploadStatus == UploadStatus.PUBLISHED }
    val pendingComments = comments.filter { !it.isAnswered }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Channel Banner & Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = channel.bannerUrl,
                    contentDescription = "Banner del canal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                            )
                        )
                )

                // Channel Info Card Floating
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = channel.avatarUrl,
                            contentDescription = channel.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(16.dp)
                            )
                        }
                        Text(
                            text = "${String.format("%,d", channel.subscribersCount)} suscriptores • ${channel.totalVideos} videos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Period filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsPeriod.values().forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { viewModel.setAnalyticsPeriod(p) },
                        label = { Text(p.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YouTubeRed.copy(alpha = 0.15f),
                            selectedLabelColor = YouTubeRed
                        )
                    )
                }
            }
        }

        // Metrics Grid (Views, Watch Time, Net Subs, Revenue)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        title = "Vistas",
                        value = analytics.viewsTrend.value,
                        trendPercent = analytics.viewsTrend.growthPercent,
                        icon = Icons.Outlined.Visibility,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Tiempo de Repr.",
                        value = analytics.watchHoursTrend.value,
                        trendPercent = analytics.watchHoursTrend.growthPercent,
                        icon = Icons.Outlined.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        title = "Nuevos Subs",
                        value = "+${String.format("%,d", analytics.netSubscribers)}",
                        trendPercent = +19.5,
                        icon = Icons.Outlined.PersonAdd,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Ingresos Estimados",
                        value = "$${String.format("%.2f", analytics.estimatedRevenueUsd)}",
                        trendPercent = +22.0,
                        icon = Icons.Outlined.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI Studio Quick Highlight Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(BartoScreen.AI_STUDIO) }
                    .border(1.dp, GeminiPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("dashboard_ai_card"),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GeminiPurple.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(GeminiPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Barto AI Studio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiPurpleGlow
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = GeminiPurple.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Gemini 3.8",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = GeminiPurpleGlow,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Genera títulos con alto CTR, diagnóstica retención y crea respuestas inteligentes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Abrir AI Studio",
                            tint = GeminiPurpleGlow
                        )
                    }
                }
            }
        }

        // Quick Tools Horizontal Grid
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "HERRAMIENTAS DEL CREADOR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    QuickToolCard(
                        title = "Publicar Video",
                        subtitle = "Subir o programar",
                        icon = Icons.Default.AddCircle,
                        accentColor = YouTubeRed,
                        onClick = { onNavigate(BartoScreen.PUBLISH_VIDEO) }
                    )
                }
                item {
                    QuickToolCard(
                        title = "Shorts Studio",
                        subtitle = "Retención & bucles",
                        icon = Icons.Default.FlashOn,
                        accentColor = NeonCyan,
                        onClick = { onNavigate(BartoScreen.SHORTS) }
                    )
                }
                item {
                    QuickToolCard(
                        title = "Bandeja (${pendingComments.size})",
                        subtitle = "Respuestas con IA",
                        icon = Icons.Default.Forum,
                        accentColor = NeonAmber,
                        onClick = { onNavigate(BartoScreen.INBOX) }
                    )
                }
                item {
                    QuickToolCard(
                        title = "Playlists",
                        subtitle = "Organizar videos",
                        icon = Icons.Default.PlaylistPlay,
                        accentColor = GeminiPurple,
                        onClick = { onNavigate(BartoScreen.PLAYLISTS) }
                    )
                }
                item {
                    QuickToolCard(
                        title = "Calendario",
                        subtitle = "${scheduledPosts.size} programados",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = NeonEmerald,
                        onClick = { onNavigate(BartoScreen.CALENDAR) }
                    )
                }
                item {
                    QuickToolCard(
                        title = "Competidores",
                        subtitle = "Análisis de canales",
                        icon = Icons.Default.CompareArrows,
                        accentColor = Color(0xFF38BDF8),
                        onClick = { onNavigate(BartoScreen.COMPETITORS) }
                    )
                }
            }
        }

        // Latest Video Performance Card
        if (latestVideo != null) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RENDIMIENTO DEL ÚLTIMO VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { onNavigate(BartoScreen.VIDEOS) }) {
                        Text("Ver todos", fontSize = 12.sp)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Thumbnail Box
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (latestVideo.videoType == VideoType.SHORT) Icons.Default.FlashOn else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(30.dp)
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    color = Color.Black.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = latestVideo.durationFormatted,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = latestVideo.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Publicado: ${latestVideo.publishDate} • Ranking 1/10",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VideoMiniStat("Vistas", String.format("%,d", latestVideo.views))
                            VideoMiniStat("CTR Miniatura", "${latestVideo.ctrPercent}%")
                            VideoMiniStat("Retención Media", latestVideo.avgViewDurationFormatted)
                            VideoMiniStat("Likes", String.format("%,d", latestVideo.likes))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // AI Diagnosis CTA
                        Button(
                            onClick = {
                                viewModel.selectVideo(latestVideo)
                                viewModel.diagnoseVideoWithAi(latestVideo)
                                onNavigate(BartoScreen.AI_STUDIO)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diagnosticar rendimiento con Gemini IA")
                        }
                    }
                }
            }
        }

        // Recent Comments Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMENTARIOS RECIENTES (${comments.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { onNavigate(BartoScreen.INBOX) }) {
                    Text("Ir a Bandeja", fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comments.take(3).forEach { comment ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.authorName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = comment.authorName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (comment.isImportantQuestion) {
                                    Surface(
                                        color = NeonAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Pregunta clave",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = NeonAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = comment.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )

                                TextButton(
                                    onClick = { onNavigate(BartoScreen.INBOX) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Reply,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Responder", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    trendPercent: Double,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trendPercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (trendPercent >= 0) NeonEmerald else NeonRose,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (trendPercent >= 0) "+" else ""}$trendPercent%",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = if (trendPercent >= 0) NeonEmerald else NeonRose,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VideoMiniStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
