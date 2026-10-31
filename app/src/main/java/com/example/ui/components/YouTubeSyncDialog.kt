package com.example.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BartoViewModel
import com.example.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeSyncDialog(
    viewModel: BartoViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    var manualToken by remember { mutableStateOf("") }
    var showTokenInput by remember { mutableStateOf(false) }

    // Launcher nativo para la pantalla de consentimiento de Google Play Services
    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleAuthorizationResult(
                context = context,
                data = result.data,
                onSuccess = { token ->
                    Toast.makeText(context, "Autorización de YouTube concedida", Toast.LENGTH_SHORT).show()
                    viewModel.syncWithYouTube(token)
                },
                onFailure = { error ->
                    Toast.makeText(context, "Error procesando autorización: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Autorización cancelada o no completada (Código: ${result.resultCode})", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Sincronización en Vivo con YouTube",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Google Identity Services (AuthorizationClient)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Info Box - Android Native Client Specs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CONFIGURACIÓN CLIENTE OAUTH ANDROID NATIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Proyecto Cloud: gen-lang-client-0381474779", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("• Client ID Android: 363588858437-1b2gba4icmt6ggp7p2d8dhve0cbmtojq.apps.googleusercontent.com", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("• Package Name: com.aistudio.bartoyoutube.qzwkmp", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("• Huella SHA-1: F7:FF:B9:89:83:91:0A:8D:87:70:E4:CC:42:B5:C3:A5:45:BE:17:7F", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("• Mecanismo: Identity.getAuthorizationClient (Nativo)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("• Scopes: youtube, youtube.readonly, youtube.upload, yt-analytics.readonly", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Native Google Identity Authorization
            Button(
                onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        viewModel.startNativeAuthorization(
                            activity = activity,
                            onResolutionRequired = { pendingIntent ->
                                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                                authorizationLauncher.launch(request)
                            },
                            onSuccess = { token ->
                                Toast.makeText(context, "Conectado. Sincronizando canal...", Toast.LENGTH_SHORT).show()
                                viewModel.syncWithYouTube(token)
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Fallo de conexión nativa: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Actividad no disponible", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_oauth_browser_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("1. Conectar con Google / YouTube (Nativo)")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manual Token Toggle Option
            TextButton(
                onClick = { showTokenInput = !showTokenInput },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = if (showTokenInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showTokenInput) "Ocultar ingreso manual de Token" else "¿Tienes un Access Token? Ingresarlo directamente",
                    fontSize = 12.sp
                )
            }

            if (showTokenInput) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "TOKEN DE ACCESO OAUTH (BEARER TOKEN)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = manualToken,
                            onValueChange = { manualToken = it },
                            placeholder = { Text("ya29.a0...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_token_input"),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                        if (!clip.isNullOrBlank()) {
                                            manualToken = clip.trim()
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Pegar")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Token de OAuth 2.0 con permisos de YouTube v3.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Step 2: Execute Live API call
            Button(
                onClick = {
                    viewModel.syncWithYouTube(manualToken.trim().ifBlank { null })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_youtube_sync_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                enabled = syncState !is SyncState.Loading
            ) {
                if (syncState is SyncState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Llamando a YouTube Data API...")
                } else {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. Consultar canal real (channels.list mine=true)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Display Section
            when (val state = syncState) {
                is SyncState.Idle -> {
                    Text(
                        text = "Presiona 'Consultar canal real' para enviar la petición a https://www.googleapis.com/youtube/v3/channels.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                is SyncState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = YouTubeRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ejecutando llamada HTTP en vivo...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("GET /youtube/v3/channels?mine=true", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                is SyncState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = YouTubeRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RESPUESTA REAL DE YOUTUBE API",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YouTubeRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Diagnóstico técnico: Comprueba que el proyecto en Google Cloud tenga 'YouTube Data API v3' habilitada y que el token de acceso contenga el alcance 'https://www.googleapis.com/auth/youtube.readonly'.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is SyncState.Success -> {
                    val result = state.result
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = NeonEmerald.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡CANAL REAL SINCRONIZADO EN VIVO!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("• Nombre del Canal: ${result.channelName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• ID del Canal en YouTube: ${result.channelId}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• Suscriptores reales: ${result.subscribers}", fontSize = 11.sp)
                            Text("• Videos públicos/cargados: ${result.videoCount}", fontSize = 11.sp)
                            Text("• Reproducciones totales: ${result.viewCount}", fontSize = 11.sp)
                            Text("• Playlists importadas: ${result.playlistsCount}", fontSize = 11.sp)
                            Text("• Comentarios sincronizados: ${result.commentsCount}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
