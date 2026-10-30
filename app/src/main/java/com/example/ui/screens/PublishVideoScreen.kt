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
import com.example.model.VideoType
import com.example.model.VisibilityType
import com.example.ui.theme.*
import com.example.viewmodel.BartoScreen
import com.example.viewmodel.BartoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishVideoScreen(
    viewModel: BartoViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tagsString by remember { mutableStateOf("youtube, barto ai, tutorial") }
    var hashtagsString by remember { mutableStateOf("#BartoAI #YouTube #Creadores") }
    var videoType by remember { mutableStateOf(VideoType.REGULAR) }
    var visibility by remember { mutableStateOf(VisibilityType.PUBLIC) }
    var category by remember { mutableStateOf("Ciencia y Tecnología") }
    var scheduleDate by remember { mutableStateOf("31/10/2026") }
    var scheduleTime by remember { mutableStateOf("18:00") }

    val uploadState by viewModel.uploadProgress.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiTitles by viewModel.aiGeneratedTitles.collectAsState()
    val aiDescription by viewModel.aiGeneratedDescription.collectAsState()
    val aiTags by viewModel.aiGeneratedTags.collectAsState()

    // Sync AI generated contents if available
    LaunchedEffect(aiDescription) {
        if (aiDescription.isNotBlank() && description.isBlank()) {
            description = aiDescription
        }
    }
    LaunchedEffect(aiTags) {
        if (aiTags.first.isNotEmpty()) {
            tagsString = aiTags.first.joinToString(", ")
            hashtagsString = aiTags.second.joinToString(" ")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar Nuevo Contenido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (uploadState.isUploading) {
            // Live Upload and Processing Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadState.progressPercent / 100f },
                            modifier = Modifier.size(80.dp),
                            color = YouTubeRed,
                            strokeWidth = 7.dp,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "${uploadState.progressPercent}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uploadState.currentPhase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        LinearProgressIndicator(
                            progress = { uploadState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (uploadState.isComplete) NeonEmerald else YouTubeRed
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Video Type Selector (Regular Video vs Short)
                item {
                    Text(
                        text = "TIPO DE PUBLICACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { videoType = VideoType.REGULAR }
                                .border(
                                    width = if (videoType == VideoType.REGULAR) 1.5.dp else 1.dp,
                                    color = if (videoType == VideoType.REGULAR) YouTubeRed else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (videoType == VideoType.REGULAR) YouTubeRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = YouTubeRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Video tradicional", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Horizontal 16:9", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { videoType = VideoType.SHORT }
                                .border(
                                    width = if (videoType == VideoType.SHORT) 1.5.dp else 1.dp,
                                    color = if (videoType == VideoType.SHORT) NeonCyan else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (videoType == VideoType.SHORT) NeonCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("YouTube Short", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Vertical 9:16 (<60s)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Title with AI Generator Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TÍTULO DEL VIDEO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                val topic = if (title.isNotBlank()) title else "Inteligencia artificial y productividad"
                                viewModel.generateAiTitles(topic, category)
                            },
                            enabled = !isAiLoading
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = GeminiPurple)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generar con IA", fontSize = 12.sp, color = GeminiPurple)
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Ej. Cómo automatizar tu canal con IA") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Suggested AI Titles Chips
                    if (aiTitles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Títulos sugeridos por Gemini:",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = GeminiPurpleGlow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            aiTitles.take(3).forEach { suggested ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { title = suggested },
                                    color = GeminiPurple.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = GeminiPurple)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(suggested, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                // Description with AI Generator Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DESCRIPCIÓN OPTIMIZADA SEO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                val t = if (title.isNotBlank()) title else "Guía de crecimiento en YouTube"
                                viewModel.generateAiDescription(t, category, tagsString)
                            },
                            enabled = !isAiLoading
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = GeminiPurple)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Redactar con IA", fontSize = 12.sp, color = GeminiPurple)
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Escribe la descripción, enlaces y marcas de tiempo...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("publish_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )
                }

                // Tags & Hashtags with AI button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ETIQUETAS & HASHTAGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                val t = if (title.isNotBlank()) title else "Contenido de tecnologia"
                                viewModel.generateAiTags(t, category)
                            },
                            enabled = !isAiLoading
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = GeminiPurple)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Etiquetas IA", fontSize = 12.sp, color = GeminiPurple)
                        }
                    }

                    OutlinedTextField(
                        value = tagsString,
                        onValueChange = { tagsString = it },
                        label = { Text("Etiquetas (separadas por comas)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = hashtagsString,
                        onValueChange = { hashtagsString = it },
                        label = { Text("Hashtags (#...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Visibility & Schedule
                item {
                    Text(
                        text = "CONFIGURACIÓN DE VISIBILIDAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
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

                    if (visibility == VisibilityType.SCHEDULED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = GeminiPurple.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = GeminiPurple)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Programado: $scheduleDate a las $scheduleTime hs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Publish CTA Button
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val finalTitle = if (title.isNotBlank()) title else "Nuevo video de Barto AI"
                            val finalDesc = if (description.isNotBlank()) description else "Video publicado desde Barto YouTube AI Studio."
                            val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val hashtags = hashtagsString.split(" ").map { it.trim() }.filter { it.isNotBlank() }

                            viewModel.publishVideo(
                                title = finalTitle,
                                description = finalDesc,
                                tags = tags,
                                hashtags = hashtags,
                                visibility = visibility,
                                videoType = videoType,
                                category = category,
                                scheduledDate = if (visibility == VisibilityType.SCHEDULED) scheduleDate else null,
                                scheduledTime = if (visibility == VisibilityType.SCHEDULED) scheduleTime else null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_publish_video_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (visibility == VisibilityType.SCHEDULED) "Programar Publicación" else "Publicar en YouTube Ahora",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
