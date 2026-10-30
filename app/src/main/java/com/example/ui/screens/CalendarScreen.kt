package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScheduledPost
import com.example.model.VideoType
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BartoScreen
import com.example.viewmodel.BartoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: BartoViewModel,
    onNavigate: (BartoScreen) -> Unit
) {
    val scheduledPosts by viewModel.scheduledPosts.collectAsState()
    var editingPost by remember { mutableStateOf<ScheduledPost?>(null) }
    var rescheduleDate by remember { mutableStateOf("") }
    var rescheduleTime by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(BartoScreen.PUBLISH_VIDEO) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Programar Video") },
                containerColor = YouTubeRed,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("schedule_video_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("calendar_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GeminiPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALENDARIO EDITORIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = GeminiPurple,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = GeminiPurple.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${scheduledPosts.size} publicaciones pendientes",
                                    fontSize = 11.sp,
                                    color = GeminiPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Visualiza tus fechas de publicación, reprograma lanzamientos y asegura la consistencia de tu canal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "PRÓXIMAS PUBLICACIONES PROGRAMADAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(scheduledPosts, key = { it.id }) { post ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Badge Box
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (post.videoType == VideoType.SHORT) NeonCyan.copy(alpha = 0.15f) else YouTubeRed.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (post.videoType == VideoType.SHORT) Icons.Default.FlashOn else Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (post.videoType == VideoType.SHORT) NeonCyan else YouTubeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = post.scheduledTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fecha: ${post.scheduledDate} a las ${post.scheduledTime} hs • ${post.visibility.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                editingPost = post
                                rescheduleDate = post.scheduledDate
                                rescheduleTime = post.scheduledTime
                            }) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Reprogramar", tint = GeminiPurple)
                            }
                            IconButton(onClick = { viewModel.deleteScheduledPost(post.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingPost != null) {
        ModalBottomSheet(
            onDismissRequest = { editingPost = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Reprogramar Fecha de Publicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rescheduleDate,
                    onValueChange = { rescheduleDate = it },
                    label = { Text("Nueva fecha (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rescheduleTime,
                    onValueChange = { rescheduleTime = it },
                    label = { Text("Nueva hora (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.reschedulePost(editingPost!!.id, rescheduleDate, rescheduleTime)
                        editingPost = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Reprogramación")
                }
            }
        }
    }
}
