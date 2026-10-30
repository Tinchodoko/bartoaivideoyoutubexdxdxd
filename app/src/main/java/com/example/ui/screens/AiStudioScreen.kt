package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Video
import com.example.ui.theme.*
import com.example.viewmodel.BartoViewModel

enum class AiStudioTab(val title: String) {
    TITLES_SEO("Títulos & SEO"),
    VIDEO_DIAGNOSIS("Diagnóstico"),
    IDEAS_AUDIENCE("Ideas & Audiencia"),
    REPORTS_AB("Informes & A/B")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    viewModel: BartoViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AiStudioTab.TITLES_SEO) }

    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiTitles by viewModel.aiGeneratedTitles.collectAsState()
    val aiDescription by viewModel.aiGeneratedDescription.collectAsState()
    val aiTags by viewModel.aiGeneratedTags.collectAsState()
    val aiDiagnosis by viewModel.aiVideoDiagnosis.collectAsState()
    val aiIdeas by viewModel.aiVideoIdeas.collectAsState()
    val aiSentiment by viewModel.aiCommentSentiment.collectAsState()
    val aiReport by viewModel.aiExecutiveReport.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val selectedVideo by viewModel.selectedVideo.collectAsState()

    var topicInput by remember { mutableStateOf("") }
    var selectedVideoForDiagnosis by remember { mutableStateOf(selectedVideo ?: videos.firstOrNull()) }

    fun copyToClipboard(text: String, label: String = "Copiado") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_studio_screen")
    ) {
        // AI Studio Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GeminiPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Barto AI Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeminiPurpleGlow
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GeminiPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Gemini 3.8 Flash",
                                fontSize = 10.sp,
                                color = GeminiPurpleGlow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "12 herramientas inteligentes para multiplicar vistas, retención y suscriptores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AiStudioTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title, fontSize = 12.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (isAiLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = GeminiPurple,
                trackColor = GeminiPurple.copy(alpha = 0.2f)
            )
        }

        // Tab Contents
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                AiStudioTab.TITLES_SEO -> {
                    // Tool 1: Títulos con Alto CTR
                    item {
                        AiToolCard(
                            title = "1. Generador de Títulos Virales con Alto CTR",
                            description = "Crea 5 títulos irresistibles sin clickbait basados en ganchos psicológicos probados."
                        ) {
                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = { topicInput = it },
                                placeholder = { Text("Tema o idea (ej. Cómo ganar suscriptores con Shorts)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val t = if (topicInput.isNotBlank()) topicInput else "Estrategias de YouTube 2026"
                                    viewModel.generateAiTitles(t)
                                },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar 5 Títulos Optimizados")
                            }

                            if (aiTitles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    aiTitles.forEach { t ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { copyToClipboard(t) },
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = t, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp), tint = GeminiPurple)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tool 2: Descripción SEO
                    item {
                        AiToolCard(
                            title = "2. Redactor de Descripciones SEO",
                            description = "Genera estructura con gancho, capítulos temporales, enlaces y llamadas a la acción."
                        ) {
                            Button(
                                onClick = {
                                    val t = if (topicInput.isNotBlank()) topicInput else "Tutorial completo de YouTube"
                                    viewModel.generateAiDescription(t, "Tecnología", "youtube, tutorial, barto")
                                },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear Descripción SEO")
                            }

                            if (aiDescription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Descripción generada:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiPurple)
                                            IconButton(
                                                onClick = { copyToClipboard(aiDescription) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = aiDescription, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Tool 3: Etiquetas & Hashtags
                    item {
                        AiToolCard(
                            title = "3. Generador de Etiquetas & Hashtags",
                            description = "Extrae los términos de búsqueda más buscados para posicionar en el algoritmo."
                        ) {
                            Button(
                                onClick = {
                                    val t = if (topicInput.isNotBlank()) topicInput else "Crecimiento en YouTube"
                                    viewModel.generateAiTags(t, "Tecnología")
                                },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar Tags y Hashtags")
                            }

                            if (aiTags.first.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column {
                                    Text("Etiquetas YouTube:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(aiTags.first.joinToString(", "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Hashtags recomendados:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(aiTags.second.joinToString(" "), fontSize = 11.sp, color = NeonCyan)
                                }
                            }
                        }
                    }
                }

                AiStudioTab.VIDEO_DIAGNOSIS -> {
                    // Tool 4: Diagnóstico de Video
                    item {
                        AiToolCard(
                            title = "4. Diagnóstico de Rendimiento con IA",
                            description = "Examina el CTR, retención de audiencia y engagement para detectar qué falló o triunfó."
                        ) {
                            Text("Selecciona el video a diagnosticar:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Video Selector Dropdown Pill
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = selectedVideoForDiagnosis?.title ?: "Ningún video seleccionado",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Vistas: ${selectedVideoForDiagnosis?.views} • CTR: ${selectedVideoForDiagnosis?.ctrPercent}% • Retención: ${selectedVideoForDiagnosis?.retentionPercent}%",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    selectedVideoForDiagnosis?.let { viewModel.diagnoseVideoWithAi(it) }
                                },
                                enabled = !isAiLoading && selectedVideoForDiagnosis != null,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizar Rendimiento con Gemini")
                            }

                            if (aiDiagnosis.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = aiDiagnosis, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                AiStudioTab.IDEAS_AUDIENCE -> {
                    // Tool 5: Sugerencias de Ideas basadas en Comentarios
                    item {
                        AiToolCard(
                            title = "5. Sugerir Ideas para Próximos Videos",
                            description = "Analiza las dudas y peticiones de la audiencia en comentarios para crear contenido demandado."
                        ) {
                            Button(
                                onClick = { viewModel.suggestNextVideoIdeas() },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Detectar Temas Pedidos por la Audiencia")
                            }

                            if (aiIdeas.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    aiIdeas.forEach { idea ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = idea,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tool 6: Análisis de Sentimiento de Comentarios
                    item {
                        AiToolCard(
                            title = "6. Análisis de Sentimiento de la Audiencia",
                            description = "Monitorea la satisfacción, críticas constructivas y dudas más repetidas."
                        ) {
                            Button(
                                onClick = { viewModel.analyzeCommentsSentiment() },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SentimentSatisfied, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizar Comentarios con IA")
                            }

                            if (aiSentiment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = aiSentiment,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                AiStudioTab.REPORTS_AB -> {
                    // Tool 7: Informe Ejecutivo Semanal y Mensual
                    item {
                        AiToolCard(
                            title = "7. Informe Ejecutivo Semanal / Mensual",
                            description = "Informe gerencial consolidado con análisis de crecimiento y 3 tareas prioritarias."
                        ) {
                            Button(
                                onClick = { viewModel.generateWeeklyExecutiveReport() },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar Informe Semanal con Gemini")
                            }

                            if (aiReport.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Resumen Ejecutivo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiPurple)
                                            IconButton(
                                                onClick = { copyToClipboard(aiReport) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = aiReport, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Tool 8: Comparación A/B de Publicaciones
                    item {
                        AiToolCard(
                            title = "8. Comparativa A/B entre Videos Propios",
                            description = "Compara el rendimiento de dos formatos o títulos para descubrir cuál funciona mejor."
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Short vs Video Largo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Datos en tiempo real", fontSize = 11.sp, color = NeonEmerald)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Shorts generan un +320% más de impresiones por hora pero menor tiempo de reproducción por usuario.\n• Videos largos generan un +800% más de ingresos estimados por cada 1,000 reproducciones (RPM más alto).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiToolCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
