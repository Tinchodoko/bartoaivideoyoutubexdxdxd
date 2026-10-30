package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.CompetitorChannel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BartoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitorsScreen(
    viewModel: BartoViewModel
) {
    val competitors by viewModel.competitors.collectAsState()
    val activeChannel by viewModel.activeChannel.collectAsState()

    var showAddModal by remember { mutableStateOf(false) }
    var compName by remember { mutableStateOf("") }
    var compHandle by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddModal = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Canal") },
                containerColor = YouTubeRed,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_competitor_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("competitors_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BENCHMARKING & CANALES DE REFERENCIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Monitorea canales públicos de tu nicho para comparar frecuencia de publicación, promedio de reproducciones y temáticas con mayor tracción.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // My Channel Benchmark Row
            item {
                Text(
                    text = "TU CANAL (PUNTO DE REFERENCIA)",
                    style = MaterialTheme.typography.labelSmall,
                    color = YouTubeRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, YouTubeRed, RoundedCornerShape(12.dp)),
                    color = YouTubeRed.copy(alpha = 0.08f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activeChannel.name, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("(${activeChannel.handle})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Suscriptores", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${activeChannel.subscribersCount / 1000}k", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Videos totales", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${activeChannel.totalVideos}", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Prom. Vistas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("51.5k", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Frecuencia", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("2.0/sem", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "CANALES DE REFERENCIA GUARDADOS (${competitors.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(competitors, key = { it.id }) { comp ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comp.name.take(1), fontWeight = FontWeight.Bold, color = NeonCyan)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(comp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(comp.handle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(onClick = { viewModel.removeCompetitor(comp.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = NeonRose, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Suscriptores", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${comp.subscribersCount / 1000}k", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Videos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${comp.videosCount}", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Prom. Vistas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${comp.avgViewsPerVideo / 1000}k", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Frecuencia", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${comp.uploadFrequencyWeekly}/sem", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Temas clave: ${comp.topKeywords.joinToString(" • ")}",
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }

    if (showAddModal) {
        ModalBottomSheet(
            onDismissRequest = { showAddModal = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Añadir Canal de Referencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = compName,
                    onValueChange = { compName = it },
                    label = { Text("Nombre del canal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = compHandle,
                    onValueChange = { compHandle = it },
                    label = { Text("Handle (ej. @creador)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (compName.isNotBlank()) {
                            val handle = if (compHandle.startsWith("@")) compHandle else "@$compHandle"
                            viewModel.addCompetitor(compName, handle)
                            showAddModal = false
                            compName = ""
                            compHandle = ""
                        }
                    },
                    enabled = compName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Añadir a la comparativa")
                }
            }
        }
    }
}
