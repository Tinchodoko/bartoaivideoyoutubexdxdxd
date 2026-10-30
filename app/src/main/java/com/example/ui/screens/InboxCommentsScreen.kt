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
import com.example.model.Comment
import com.example.ui.theme.*
import com.example.viewmodel.BartoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxCommentsScreen(
    viewModel: BartoViewModel
) {
    val comments by viewModel.comments.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val searchQuery by viewModel.commentSearchQuery.collectAsState()
    val pendingOnly by viewModel.commentFilterPendingOnly.collectAsState()
    val spamOnly by viewModel.commentFilterSpamOnly.collectAsState()
    val selectedVideoFilter by viewModel.selectedVideoFilterForComments.collectAsState()
    val aiSuggestedReply by viewModel.aiSuggestedReply.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var replyingCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    // When AI generates a reply, automatically prefill the text field
    LaunchedEffect(aiSuggestedReply) {
        if (aiSuggestedReply.isNotBlank() && replyingCommentId != null) {
            replyText = aiSuggestedReply
        }
    }

    // Filter comments
    val filteredComments = comments.filter { comment ->
        val matchesQuery = comment.text.contains(searchQuery, ignoreCase = true) ||
                comment.authorName.contains(searchQuery, ignoreCase = true) ||
                comment.videoTitle.contains(searchQuery, ignoreCase = true)
        val matchesPending = if (pendingOnly) !comment.isAnswered else true
        val matchesSpam = if (spamOnly) comment.isSpamOrOffensive else true
        val matchesVideo = selectedVideoFilter == null || comment.videoId == selectedVideoFilter
        matchesQuery && matchesPending && matchesSpam && matchesVideo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inbox_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setCommentSearchQuery(it) },
            placeholder = { Text("Buscar en comentarios o autores...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setCommentSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("comment_search_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = !pendingOnly && !spamOnly && selectedVideoFilter == null,
                    onClick = {
                        if (pendingOnly) viewModel.toggleCommentFilterPending()
                        if (spamOnly) viewModel.toggleCommentFilterSpam()
                        viewModel.filterCommentsByVideo(null)
                    },
                    label = { Text("Todos (${comments.size})", fontSize = 12.sp) }
                )
            }
            item {
                FilterChip(
                    selected = pendingOnly,
                    onClick = { viewModel.toggleCommentFilterPending() },
                    label = { Text("Pendientes de respuesta", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = YouTubeRed.copy(alpha = 0.15f),
                        selectedLabelColor = YouTubeRed
                    )
                )
            }
            item {
                FilterChip(
                    selected = spamOnly,
                    onClick = { viewModel.toggleCommentFilterSpam() },
                    label = { Text("Spam / Ofensivos", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonRose.copy(alpha = 0.15f),
                        selectedLabelColor = NeonRose
                    )
                )
            }
            if (selectedVideoFilter != null) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.filterCommentsByVideo(null) },
                        label = { Text("Video filtrado ✕", fontSize = 12.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Comments List
        if (filteredComments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Al día! No hay comentarios en esta bandeja.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredComments, key = { it.id }) { comment ->
                    CommentInboxCard(
                        comment = comment,
                        isReplying = replyingCommentId == comment.id,
                        replyText = replyText,
                        onReplyTextChange = { replyText = it },
                        isAiLoading = isAiLoading,
                        onStartReplying = {
                            replyingCommentId = comment.id
                            replyText = ""
                        },
                        onCancelReplying = {
                            replyingCommentId = null
                            replyText = ""
                        },
                        onSendReply = {
                            viewModel.replyToComment(comment.id, replyText)
                            replyingCommentId = null
                            replyText = ""
                        },
                        onGenerateAiReply = {
                            viewModel.generateSuggestedReply(comment.text, comment.videoTitle)
                        },
                        onToggleHeart = { viewModel.toggleCreatorHeart(comment.id) },
                        onToggleFlag = { viewModel.toggleFlagForReview(comment.id) },
                        onDeleteComment = { viewModel.deleteComment(comment.id) },
                        onHideUser = { viewModel.requestHideUser(comment.authorName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInboxCard(
    comment: Comment,
    isReplying: Boolean,
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    isAiLoading: Boolean,
    onStartReplying: () -> Unit,
    onCancelReplying: () -> Unit,
    onSendReply: () -> Unit,
    onGenerateAiReply: () -> Unit,
    onToggleHeart: () -> Unit,
    onToggleFlag: () -> Unit,
    onDeleteComment: () -> Unit,
    onHideUser: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (comment.isSpamOrOffensive) NeonRose.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp)
            )
            .testTag("comment_card_${comment.id}"),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author & Video Context
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.authorName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = YouTubeRed,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.authorName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (comment.isSpamOrOffensive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NeonRose.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Spam sospechoso",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = NeonRose,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "En: ${comment.videoTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Options menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (comment.isFlaggedForReview) "Desmarcar revisión" else "Marcar para revisión") },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onToggleFlag()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ocultar usuario del canal", color = NeonRose) },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = NeonRose) },
                            onClick = {
                                menuExpanded = false
                                onHideUser()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar comentario", color = NeonRose) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = NeonRose) },
                            onClick = {
                                menuExpanded = false
                                onDeleteComment()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comment text
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata & Action bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Likes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${comment.likesCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Creator Heart Button
                    IconButton(
                        onClick = onToggleHeart,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.hasCreatorHeart) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Corazón del creador",
                            tint = if (comment.hasCreatorHeart) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (!isReplying) {
                    Button(
                        onClick = onStartReplying,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Icon(imageVector = Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Responder", fontSize = 12.sp)
                    }
                }
            }

            // Existing replies history
            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    comment.replies.forEach { rep ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = rep.authorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = YouTubeRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rep.timestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = rep.text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // In-line Reply Box with Gemini AI Generator
            if (isReplying) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Respondiendo como Creador",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = YouTubeRed
                            )

                            // AI Generator Trigger
                            TextButton(
                                onClick = onGenerateAiReply,
                                enabled = !isAiLoading,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = GeminiPurple)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sugerir con Gemini IA", fontSize = 11.sp, color = GeminiPurple)
                            }
                        }

                        OutlinedTextField(
                            value = replyText,
                            onValueChange = onReplyTextChange,
                            placeholder = { Text("Escribe tu respuesta...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onCancelReplying) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onSendReply,
                                enabled = replyText.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                            ) {
                                Text("Enviar")
                            }
                        }
                    }
                }
            }
        }
    }
}
