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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UploadStatus
import com.example.model.Video
import com.example.model.VideoType
import com.example.model.VisibilityType
import com.example.ui.theme.*
import com.example.viewmodel.BartoScreen
import com.example.viewmodel.BartoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    viewModel: BartoViewModel,
    onNavigate: (BartoScreen) -> Unit
) {
    val videos by viewModel.videos.collectAsState()
    val searchQuery by viewModel.videoSearchQuery.collectAsState()
    val visibilityFilter by viewModel.videoFilterVisibility.collectAsState()

    var editingVideo by remember { mutableStateOf<Video?>(null) }

    // Filter list
    val filteredVideos = videos.filter { video ->
        val matchesQuery = video.title.contains(searchQuery, ignoreCase = true) ||
                video.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesVisibility = visibilityFilter == null || video.visibility == visibilityFilter
        matchesQuery && matchesVisibility
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(BartoScreen.PUBLISH_VIDEO) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Publicar") },
                containerColor = YouTubeRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("publish_video_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("videos_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setVideoSearchQuery(it) },
                placeholder = { Text("Buscar por título o etiquetas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setVideoSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("video_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Visibility Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = visibilityFilter == null,
                        onClick = { viewModel.setVideoFilterVisibility(null) },
                        label = { Text("Todos (${videos.size})", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = visibilityFilter == VisibilityType.PUBLIC,
                        onClick = { viewModel.setVideoFilterVisibility(VisibilityType.PUBLIC) },
                        label = { Text("Públicos", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = visibilityFilter == VisibilityType.UNLISTED,
                        onClick = { viewModel.setVideoFilterVisibility(VisibilityType.UNLISTED) },
                        label = { Text("No listados", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = visibilityFilter == VisibilityType.PRIVATE,
                        onClick = { viewModel.setVideoFilterVisibility(VisibilityType.PRIVATE) },
                        label = { Text("Privados / Borradores", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = visibilityFilter == VisibilityType.SCHEDULED,
                        onClick = { viewModel.setVideoFilterVisibility(VisibilityType.SCHEDULED) },
                        label = { Text("Programados", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Videos List
            if (filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron videos con los filtros actuales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVideos, key = { it.id }) { video ->
                        VideoCardItem(
                            video = video,
                            onEdit = { editingVideo = video },
                            onDiagnoseWithAi = {
                                viewModel.selectVideo(video)
                                viewModel.diagnoseVideoWithAi(video)
                                onNavigate(BartoScreen.AI_STUDIO)
                            },
                            onDelete = { viewModel.requestDeleteVideo(video) }
                        )
                    }
                }
            }
        }
    }

    // Edit Video BottomSheet
    if (editingVideo != null) {
        EditVideoModal(
            video = editingVideo!!,
            onDismiss = { editingVideo = null },
            onSave = { id, title, desc, tags, visibility, category, commentsEnabled ->
                viewModel.updateVideo(id, title, desc, tags, visibility, category, commentsEnabled)
                editingVideo = null
            }
        )
    }
}

@Composable
private fun VideoCardItem(
    video: Video,
    onEdit: () -> Unit,
    onDiagnoseWithAi: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .testTag("video_card_${video.id}"),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Video Media Preview Box
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (video.videoType == VideoType.SHORT) Icons.Default.FlashOn else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(32.dp)
                    )

                    // Video Type Tag
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp),
                        color = if (video.videoType == VideoType.SHORT) NeonCyan else Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (video.videoType == VideoType.SHORT) "SHORT" else "VIDEO",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (video.videoType == VideoType.SHORT) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Duration Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.durationFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${video.publishDate} • ${video.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Visibility chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (video.visibility) {
                                VisibilityType.PUBLIC -> NeonEmerald.copy(alpha = 0.15f)
                                VisibilityType.UNLISTED -> NeonAmber.copy(alpha = 0.15f)
                                VisibilityType.PRIVATE -> NeonRose.copy(alpha = 0.15f)
                                VisibilityType.SCHEDULED -> GeminiPurple.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = video.visibility.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (video.visibility) {
                                    VisibilityType.PUBLIC -> NeonEmerald
                                    VisibilityType.UNLISTED -> NeonAmber
                                    VisibilityType.PRIVATE -> NeonRose
                                    VisibilityType.SCHEDULED -> GeminiPurpleGlow
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!video.commentsEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Comentarios desactivados",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 3-dots Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar información") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Diagnosticar con Gemini IA") },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GeminiPurple) },
                            onClick = {
                                menuExpanded = false
                                onDiagnoseWithAi()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar video", color = NeonRose) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = NeonRose) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format("%,d", video.views), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format("%,d", video.likes), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${video.commentsCount}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Percent, contentDescription = null, modifier = Modifier.size(15.dp), tint = NeonCyan)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "CTR ${video.ctrPercent}%", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVideoModal(
    video: Video,
    onDismiss: () -> Unit,
    onSave: (id: String, title: String, desc: String, tags: List<String>, visibility: VisibilityType, category: String, commentsEnabled: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(video.title) }
    var description by remember { mutableStateOf(video.description) }
    var tagsString by remember { mutableStateOf(video.tags.joinToString(", ")) }
    var visibility by remember { mutableStateOf(video.visibility) }
    var category by remember { mutableStateOf(video.category) }
    var commentsEnabled by remember { mutableStateOf(video.commentsEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar Detalles del Video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(10.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tagsString,
                onValueChange = { tagsString = it },
                label = { Text("Etiquetas (separadas por comas)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visibility Selector
            Text(text = "Visibilidad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisibilityType.values().forEach { v ->
                    FilterChip(
                        selected = visibility == v,
                        onClick = { visibility = v },
                        label = { Text(v.displayName, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comments Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Permitir comentarios", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = commentsEnabled, onCheckedChange = { commentsEnabled = it })
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(video.id, title, description, tags, visibility, category, commentsEnabled)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
