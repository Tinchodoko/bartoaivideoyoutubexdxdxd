package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.example.network.YouTubeNetworkClient

class BartoRepository(val networkClient: YouTubeNetworkClient? = null) {

    // Available Channels
    private val channel1 = Channel(
        id = "c_barto_tech",
        name = "Barto Tech & AI",
        handle = "@barto_ai",
        description = "Canal oficial sobre Inteligencia Artificial, programación, automatización y creación de contenido en YouTube.",
        avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&auto=format&fit=crop&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
        subscribersCount = 142800,
        totalVideos = 184,
        totalViews = 9482100L,
        isMonetized = true,
        watchHoursLastYear = 54200.0,
        links = listOf("Web Oficial" to "https://barto.ai", "GitHub" to "https://github.com/barto", "Discord" to "https://discord.gg/barto")
    )

    private val channel2 = Channel(
        id = "c_barto_gaming",
        name = "Barto Gaming Studio",
        handle = "@barto_gaming",
        description = "Gameplay, stream highlights y análisis técnico de lanzamientos de videojuegos.",
        avatarUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=200&auto=format&fit=crop&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&auto=format&fit=crop&q=80",
        subscribersCount = 48500,
        totalVideos = 92,
        totalViews = 3120400L,
        isMonetized = true,
        watchHoursLastYear = 22100.0,
        links = listOf("Twitch" to "https://twitch.tv/barto", "Twitter" to "https://x.com/barto_gaming")
    )

    private val channel3 = Channel(
        id = "c_barto_shorts",
        name = "Barto Shorts en 60s",
        handle = "@barto_shorts",
        description = "Píldoras rápidas de tecnología, productividad y trucos virales en formato vertical.",
        avatarUrl = "https://images.unsplash.com/photo-1534972195531-a756b1126f24?w=200&auto=format&fit=crop&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1534972195531-a756b1126f24?w=1200&auto=format&fit=crop&q=80",
        subscribersCount = 89200,
        totalVideos = 310,
        totalViews = 18400200L,
        isMonetized = true,
        watchHoursLastYear = 38400.0,
        links = listOf("TikTok" to "https://tiktok.com/@barto_shorts")
    )

    private val _availableChannels = MutableStateFlow(listOf(channel1, channel2, channel3))
    val availableChannels: StateFlow<List<Channel>> = _availableChannels.asStateFlow()

    private val _activeChannel = MutableStateFlow(channel1)
    val activeChannel: StateFlow<Channel> = _activeChannel.asStateFlow()

    // Multi-platform Architecture Registry
    private val _connectedPlatforms = MutableStateFlow(
        listOf(
            PlatformType.YOUTUBE to true,
            PlatformType.TIKTOK to false,
            PlatformType.INSTAGRAM to false,
            PlatformType.FACEBOOK to false,
            PlatformType.X_TWITTER to false,
            PlatformType.THREADS to false,
            PlatformType.LINKEDIN to false
        )
    )
    val connectedPlatforms = _connectedPlatforms.asStateFlow()

    // Videos
    private val _videos = MutableStateFlow(createInitialVideos())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    // Comments
    private val _comments = MutableStateFlow(createInitialComments())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // Playlists
    private val _playlists = MutableStateFlow(createInitialPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Scheduled Posts
    private val _scheduledPosts = MutableStateFlow(createInitialScheduled())
    val scheduledPosts: StateFlow<List<ScheduledPost>> = _scheduledPosts.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Competitors
    private val _competitors = MutableStateFlow(createInitialCompetitors())
    val competitors: StateFlow<List<CompetitorChannel>> = _competitors.asStateFlow()

    // Audit Log
    private val _auditLogs = MutableStateFlow(createInitialAuditLogs())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Hidden users
    private val _hiddenUsers = MutableStateFlow(listOf("crypto_bot_99", "free_giveaway_winner"))
    val hiddenUsers: StateFlow<List<String>> = _hiddenUsers.asStateFlow()

    // Channel Switching
    fun switchChannel(channelId: String) {
        val selected = _availableChannels.value.find { it.id == channelId } ?: return
        _activeChannel.value = selected
        logAudit("Cambio de canal", "Se seleccionó el canal ${selected.name}")
    }

    // Video Operations
    fun updateVideo(
        id: String,
        title: String,
        description: String,
        tags: List<String>,
        visibility: VisibilityType,
        category: String,
        commentsEnabled: Boolean
    ) {
        _videos.value = _videos.value.map { video ->
            if (video.id == id) {
                video.copy(
                    title = title,
                    description = description,
                    tags = tags,
                    visibility = visibility,
                    category = category,
                    commentsEnabled = commentsEnabled
                )
            } else video
        }
        logAudit("Video editado", "Se actualizaron los metadatos de '$title'")
    }

    fun deleteVideo(videoId: String) {
        val video = _videos.value.find { it.id == videoId }
        _videos.value = _videos.value.filter { it.id != videoId }
        // Also remove from playlists
        _playlists.value = _playlists.value.map { pl ->
            pl.copy(videoIds = pl.videoIds.filter { it != videoId })
        }
        logAudit("Video eliminado", "Se eliminó el video '${video?.title ?: videoId}'")
    }

    fun publishNewVideo(
        title: String,
        description: String,
        tags: List<String>,
        hashtags: List<String>,
        visibility: VisibilityType,
        videoType: VideoType,
        category: String,
        scheduledDate: String? = null,
        scheduledTime: String? = null
    ) {
        val newId = "v_" + UUID.randomUUID().toString().take(8)
        val isScheduled = scheduledDate != null && scheduledTime != null
        val newVideo = Video(
            id = newId,
            title = title,
            description = description,
            tags = tags,
            hashtags = hashtags,
            durationFormatted = if (videoType == VideoType.SHORT) "0:58" else "12:45",
            durationSeconds = if (videoType == VideoType.SHORT) 58 else 765,
            views = 0,
            likes = 0,
            dislikes = 0,
            commentsCount = 0,
            sharesCount = 0,
            impressions = 120,
            ctrPercent = 8.5,
            avgViewDurationFormatted = if (videoType == VideoType.SHORT) "0:52" else "6:30",
            retentionPercent = if (videoType == VideoType.SHORT) 89.6 else 52.0,
            publishDate = if (isScheduled) "$scheduledDate $scheduledTime" else "Hace instantes",
            visibility = if (isScheduled) VisibilityType.SCHEDULED else visibility,
            videoType = videoType,
            category = category,
            uploadStatus = if (isScheduled) UploadStatus.PUBLISHED else UploadStatus.PUBLISHED,
            scheduledPublishTime = if (isScheduled) "$scheduledDate $scheduledTime" else null
        )
        _videos.value = listOf(newVideo) + _videos.value

        if (isScheduled) {
            _scheduledPosts.value = listOf(
                ScheduledPost(
                    id = "sp_" + UUID.randomUUID().toString().take(6),
                    title = title,
                    videoType = videoType,
                    scheduledDate = scheduledDate!!,
                    scheduledTime = scheduledTime!!,
                    visibility = visibility
                )
            ) + _scheduledPosts.value
        }

        // Add Notification
        _notifications.value = listOf(
            AppNotification(
                id = "n_" + System.currentTimeMillis(),
                title = "Video publicado con éxito",
                message = "Tu video '$title' ya está disponible en YouTube (${visibility.displayName}).",
                timestamp = "Ahora",
                type = NotificationType.UPLOAD_FINISHED,
                relatedVideoId = newId
            )
        ) + _notifications.value

        logAudit("Publicación creada", "Nuevo $videoType: '$title'")
    }

    // Comment Operations
    fun replyToComment(commentId: String, replyText: String) {
        val reply = CommentReply(
            id = "rep_" + System.currentTimeMillis(),
            authorName = _activeChannel.value.name,
            text = replyText,
            timestamp = "Hace instantes",
            isCreatorReply = true
        )
        _comments.value = _comments.value.map { c ->
            if (c.id == commentId) {
                c.copy(
                    isAnswered = true,
                    replies = c.replies + reply
                )
            } else c
        }
        logAudit("Comentario respondido", "Respuesta enviada a comentario $commentId")
    }

    fun toggleCreatorHeart(commentId: String) {
        _comments.value = _comments.value.map { c ->
            if (c.id == commentId) c.copy(hasCreatorHeart = !c.hasCreatorHeart) else c
        }
    }

    fun toggleFlagForReview(commentId: String) {
        _comments.value = _comments.value.map { c ->
            if (c.id == commentId) c.copy(isFlaggedForReview = !c.isFlaggedForReview) else c
        }
    }

    fun deleteComment(commentId: String) {
        _comments.value = _comments.value.filter { it.id != commentId }
        logAudit("Comentario eliminado", "Comentario ID $commentId eliminado por el creador")
    }

    fun hideUserFromChannel(authorName: String) {
        if (!_hiddenUsers.value.contains(authorName)) {
            _hiddenUsers.value = _hiddenUsers.value + authorName
        }
        _comments.value = _comments.value.filter { it.authorName != authorName }
        logAudit("Usuario ocultado", "Se ocultó a '$authorName' del canal")
    }

    // Playlist Operations
    fun createPlaylist(title: String, description: String, visibility: VisibilityType, videoIds: List<String> = emptyList()) {
        val newPlaylist = Playlist(
            id = "pl_" + UUID.randomUUID().toString().take(6),
            title = title,
            description = description,
            visibility = visibility,
            videoIds = videoIds,
            updatedAt = "Hoy"
        )
        _playlists.value = listOf(newPlaylist) + _playlists.value
        logAudit("Lista creada", "Playlist '$title' agregada con ${videoIds.size} videos")
    }

    fun updatePlaylist(id: String, title: String, description: String, visibility: VisibilityType, videoIds: List<String>) {
        _playlists.value = _playlists.value.map { pl ->
            if (pl.id == id) {
                pl.copy(title = title, description = description, visibility = visibility, videoIds = videoIds, updatedAt = "Hoy")
            } else pl
        }
        logAudit("Lista editada", "Playlist '$title' actualizada")
    }

    fun deletePlaylist(id: String) {
        val pl = _playlists.value.find { it.id == id }
        _playlists.value = _playlists.value.filter { it.id != id }
        logAudit("Lista eliminada", "Se borró la playlist '${pl?.title ?: id}'")
    }

    // Scheduling
    fun reschedulePost(postId: String, newDate: String, newTime: String) {
        _scheduledPosts.value = _scheduledPosts.value.map { p ->
            if (p.id == postId) p.copy(scheduledDate = newDate, scheduledTime = newTime) else p
        }
        logAudit("Re-programación", "Publicación $postId reprogramada para $newDate $newTime")
    }

    fun deleteScheduledPost(postId: String) {
        _scheduledPosts.value = _scheduledPosts.value.filter { it.id != postId }
    }

    // Notifications
    fun markNotificationAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // Competitors
    fun addCompetitor(name: String, handle: String) {
        val newCompetitor = CompetitorChannel(
            id = "comp_" + UUID.randomUUID().toString().take(6),
            name = name,
            handle = handle,
            subscribersCount = (50000..350000).random(),
            videosCount = (80..400).random(),
            avgViewsPerVideo = (12000L..150000L).random(),
            uploadFrequencyWeekly = 2.5,
            topKeywords = listOf("IA", "Tutoriales", "Tech", "Productividad"),
            latestVideoTitle = "Tendencias clave en tecnología y canales de contenido"
        )
        _competitors.value = _competitors.value + newCompetitor
        logAudit("Competidor agregado", "Canal $name ($handle) sumado a la comparativa")
    }

    fun removeCompetitor(id: String) {
        _competitors.value = _competitors.value.filter { it.id != id }
    }

    // Analytics Generator based on Period
    fun getAnalytics(period: AnalyticsPeriod): ChannelAnalytics {
        val multiplier = when (period) {
            AnalyticsPeriod.DAYS_7 -> 1.0
            AnalyticsPeriod.DAYS_28 -> 3.8
            AnalyticsPeriod.DAYS_90 -> 11.5
            AnalyticsPeriod.DAYS_365 -> 44.0
        }

        val baseViews = (125000 * multiplier).toLong()
        val baseHours = 4800.0 * multiplier
        val gainedSubs = (1850 * multiplier).toInt()
        val lostSubs = (240 * multiplier).toInt()
        val netSubs = gainedSubs - lostSubs
        val revenue = 620.0 * multiplier

        return ChannelAnalytics(
            period = period,
            totalViews = baseViews,
            viewsTrend = MetricTrend(String.format("%,d", baseViews), +18.4, true),
            watchHours = baseHours,
            watchHoursTrend = MetricTrend(String.format("%,.1f h", baseHours), +14.2, true),
            currentSubscribers = _activeChannel.value.subscribersCount,
            subscribersGained = gainedSubs,
            subscribersLost = lostSubs,
            netSubscribers = netSubs,
            impressions = (baseViews * 12.5).toLong(),
            ctrPercent = 7.8,
            avgViewDuration = "4m 32s",
            estimatedRevenueUsd = revenue,
            likes = (baseViews * 0.065).toLong(),
            shares = (baseViews * 0.012).toLong(),
            comments = (baseViews * 0.008).toLong(),
            topSubscribersVideos = listOf(
                "Cómo programar con IA en 2026" to (gainedSubs * 0.35).toInt(),
                "3 Trucos de YouTube Shorts que el 99% desconoce" to (gainedSubs * 0.28).toInt(),
                "Construí una app completa en 1 hora con Gemini" to (gainedSubs * 0.22).toInt(),
                "Mi Setup de Grabación y Audio Minimalista" to (gainedSubs * 0.15).toInt()
            ),
            ageDemographics = listOf(
                "18 - 24 años" to 28.5,
                "25 - 34 años" to 44.2,
                "35 - 44 años" to 17.8,
                "45+ años" to 9.5
            ),
            topCountries = listOf(
                "México" to 29.4,
                "España" to 24.1,
                "Argentina" to 19.3,
                "Colombia" to 14.8,
                "Estados Unidos" to 7.2,
                "Otros" to 5.2
            ),
            trafficSources = listOf(
                "Feed de Shorts" to 42.0,
                "Búsqueda de YouTube" to 28.5,
                "Videos sugeridos" to 18.2,
                "Externo (Google/X)" to 6.3,
                "Funciones de exploración" to 5.0
            ),
            dailyViews = listOf(
                "Lun" to (baseViews / 7 * 0.85).toLong(),
                "Mar" to (baseViews / 7 * 0.95).toLong(),
                "Mié" to (baseViews / 7 * 1.10).toLong(),
                "Jue" to (baseViews / 7 * 1.05).toLong(),
                "Vie" to (baseViews / 7 * 1.25).toLong(),
                "Sáb" to (baseViews / 7 * 1.40).toLong(),
                "Dom" to (baseViews / 7 * 1.30).toLong()
            )
        )
    }

    suspend fun syncWithYouTubeApi(manualToken: String? = null): Result<SyncSummaryResult> {
        val client = networkClient ?: return Result.failure(IllegalStateException("Cliente de red no configurado."))

        if (!manualToken.isNullOrBlank()) {
            client.authManager.saveTokens(manualToken)
        }

        if (!client.authManager.hasValidToken()) {
            return Result.failure(
                IllegalStateException("No hay token de acceso configurado. Conecta tu cuenta con Google OAuth o ingresa un token de autorización.")
            )
        }

        return try {
            // 1. Fetch channel via channels.list with mine=true
            val channelResponse = client.apiService.getMyChannel(part = "snippet,contentDetails,statistics", mine = true)
            if (!channelResponse.isSuccessful) {
                val rawError = channelResponse.errorBody()?.string()
                val errorMsg = client.parseErrorBody(rawError)
                val code = channelResponse.code()
                val userFacingError = when (code) {
                    401 -> "Error 401 (No autorizado): El token de acceso es inválido o ha expirado. Por favor, autoriza la conexión con Google OAuth."
                    403 -> "Error 403 (Prohibido): $errorMsg. Verifica que 'YouTube Data API v3' esté habilitada en Google Cloud Console para el proyecto gen-lang-client-0381474779."
                    404 -> "Error 404 (No encontrado): No se encontró el recurso solicitado en YouTube."
                    else -> "Error HTTP $code de YouTube API: $errorMsg"
                }
                Result.failure(Exception(userFacingError))
            } else {
                val channelItems = channelResponse.body()?.items
                if (channelItems.isNullOrEmpty()) {
                    Result.failure(
                        IllegalStateException("La cuenta de Google autenticada no tiene ningún canal de YouTube creado.")
                    )
                } else {
                    val channelItem = channelItems.first()
                    val channelId = channelItem.id
                    val title = channelItem.snippet?.title ?: "Canal de YouTube"
                    val handle = channelItem.snippet?.customUrl ?: "@${title.lowercase().replace(" ", "")}"
                    val desc = channelItem.snippet?.description ?: ""
                    val avatar = channelItem.snippet?.thumbnails?.high?.url
                        ?: channelItem.snippet?.thumbnails?.medium?.url
                        ?: channelItem.snippet?.thumbnails?.default?.url
                        ?: ""
                    val subsCount = channelItem.statistics?.subscriberCount?.toIntOrNull() ?: 0
                    val videoCount = channelItem.statistics?.videoCount?.toIntOrNull() ?: 0
                    val viewsCount = channelItem.statistics?.viewCount?.toLongOrNull() ?: 0L
                    val uploadsPlaylistId = channelItem.contentDetails?.relatedPlaylists?.uploads

                    // Create real synced Channel
                    val syncedChannel = Channel(
                        id = channelId,
                        name = title,
                        handle = handle,
                        description = desc,
                        avatarUrl = avatar,
                        bannerUrl = avatar,
                        subscribersCount = subsCount,
                        totalVideos = videoCount,
                        totalViews = viewsCount,
                        isMonetized = true,
                        watchHoursLastYear = (viewsCount * 0.05).coerceAtLeast(100.0),
                        connectionStatus = "Conectado en vivo a YouTube Data API v3 ($channelId)"
                    )

                    _activeChannel.value = syncedChannel
                    _availableChannels.value = listOf(syncedChannel) + _availableChannels.value.filter { it.id != channelId }

                    // 2. Fetch real videos from uploads playlist
                    var fetchedVideosCount = 0
                    if (!uploadsPlaylistId.isNullOrBlank()) {
                        try {
                            val playlistItemsResp = client.apiService.getPlaylistItems(playlistId = uploadsPlaylistId, maxResults = 50)
                            if (playlistItemsResp.isSuccessful) {
                                val items = playlistItemsResp.body()?.items.orEmpty()
                                if (items.isNotEmpty()) {
                                    val realVideos = items.mapNotNull { pItem ->
                                        val vId = pItem.contentDetails?.videoId ?: pItem.snippet?.resourceId?.videoId ?: return@mapNotNull null
                                        val vTitle = pItem.snippet?.title ?: "Video de YouTube"
                                        val vDesc = pItem.snippet?.description ?: ""
                                        val vPublished = pItem.snippet?.publishedAt ?: "Reciente"

                                        Video(
                                            id = vId,
                                            title = vTitle,
                                            description = vDesc,
                                            tags = listOf("youtube", "canal real"),
                                            hashtags = listOf("#YouTube"),
                                            durationFormatted = "Video",
                                            durationSeconds = 600,
                                            views = (viewsCount / (videoCount.coerceAtLeast(1))).coerceAtLeast(0),
                                            likes = 0,
                                            dislikes = 0,
                                            commentsCount = 0,
                                            sharesCount = 0,
                                            impressions = 0,
                                            ctrPercent = 7.5,
                                            avgViewDurationFormatted = "4:30",
                                            retentionPercent = 50.0,
                                            publishDate = vPublished.take(10),
                                            visibility = VisibilityType.PUBLIC,
                                            videoType = VideoType.REGULAR,
                                            category = "General",
                                            isMonetized = true,
                                            estimatedRevenueUsd = 0.0
                                        )
                                    }
                                    if (realVideos.isNotEmpty()) {
                                        _videos.value = realVideos
                                        fetchedVideosCount = realVideos.size
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("BartoRepository", "Failed to fetch playlist items: ${e.message}")
                        }
                    }

                    // 3. Fetch real playlists
                    var fetchedPlaylistsCount = 0
                    try {
                        val playlistsResp = client.apiService.getMyPlaylists(mine = true, maxResults = 25)
                        if (playlistsResp.isSuccessful) {
                            val pItems = playlistsResp.body()?.items.orEmpty()
                            if (pItems.isNotEmpty()) {
                                val realPlaylists = pItems.map { plItem ->
                                    Playlist(
                                        id = plItem.id,
                                        title = plItem.snippet?.title ?: "Lista de YouTube",
                                        description = plItem.snippet?.description ?: "",
                                        visibility = VisibilityType.PUBLIC,
                                        videoIds = emptyList(),
                                        updatedAt = plItem.snippet?.publishedAt?.take(10) ?: "Hoy"
                                    )
                                }
                                _playlists.value = realPlaylists
                                fetchedPlaylistsCount = realPlaylists.size
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("BartoRepository", "Failed to fetch playlists: ${e.message}")
                    }

                    // 4. Fetch real comments
                    var fetchedCommentsCount = 0
                    try {
                        val commentsResp = client.apiService.getMyCommentThreads(channelId = channelId, maxResults = 50)
                        if (commentsResp.isSuccessful) {
                            val cItems = commentsResp.body()?.items.orEmpty()
                            if (cItems.isNotEmpty()) {
                                val realComments = cItems.mapNotNull { threadItem ->
                                    val topComment = threadItem.snippet?.topLevelComment?.snippet ?: return@mapNotNull null
                                    Comment(
                                        id = threadItem.id,
                                        videoId = threadItem.snippet.videoId ?: "",
                                        videoTitle = "Video del canal",
                                        authorName = topComment.authorDisplayName ?: "Usuario de YouTube",
                                        text = topComment.textDisplay ?: "",
                                        timestamp = topComment.publishedAt?.take(10) ?: "Reciente",
                                        likesCount = topComment.likeCount ?: 0,
                                        hasCreatorHeart = false,
                                        isAnswered = (threadItem.snippet.totalReplyCount ?: 0) > 0,
                                        isFlaggedForReview = false,
                                        isSpamOrOffensive = false,
                                        isImportantQuestion = topComment.textDisplay?.contains("?") == true
                                    )
                                }
                                if (realComments.isNotEmpty()) {
                                    _comments.value = realComments
                                    fetchedCommentsCount = realComments.size
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("BartoRepository", "Failed to fetch comments: ${e.message}")
                    }

                    logAudit(
                        "Sincronización real YouTube",
                        "Canal real '$title' ($channelId) sincronizado con $subsCount subs, $videoCount videos."
                    )

                    Result.success(
                        SyncSummaryResult(
                            channelId = channelId,
                            channelName = title,
                            subscribers = subsCount,
                            videoCount = videoCount,
                            viewCount = viewsCount,
                            playlistsCount = fetchedPlaylistsCount,
                            commentsCount = fetchedCommentsCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun logAudit(action: String, details: String) {
        val entry = AuditLogEntry(
            id = "audit_" + System.currentTimeMillis(),
            timestamp = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date()),
            action = action,
            details = details,
            channelName = _activeChannel.value.name
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(49)
    }

    // Seed Data Generators
    private fun createInitialVideos(): List<Video> {
        return listOf(
            Video(
                id = "v_101",
                title = "Cómo programar con IA en 2026: Guía completa paso a paso",
                description = "En este video tutorial exploramos cómo integrar modelos de lenguaje modernos, automatizaciones y agentes en tu flujo diario de desarrollo de software.",
                tags = listOf("programacion", "inteligencia artificial", "desarrollo", "tutorial", "ia 2026", "software"),
                hashtags = listOf("#IA", "#Programacion", "#Desarrollo", "#Tech"),
                durationFormatted = "18:42",
                durationSeconds = 1122,
                views = 124500,
                likes = 8920,
                dislikes = 45,
                commentsCount = 642,
                sharesCount = 1230,
                impressions = 1420000,
                ctrPercent = 8.8,
                avgViewDurationFormatted = "9m 14s",
                retentionPercent = 49.3,
                publishDate = "Hace 3 días",
                visibility = VisibilityType.PUBLIC,
                videoType = VideoType.REGULAR,
                category = "Ciencia y Tecnología",
                isMonetized = true,
                estimatedRevenueUsd = 412.50
            ),
            Video(
                id = "v_102",
                title = "3 Trucos de YouTube Shorts que el 99% desconoce ⚡",
                description = "Multiplica tus vistas y retención en Shorts con estas 3 reglas de gancho visual y bucle infinito.",
                tags = listOf("shorts", "youtube shorts", "trucos youtube", "crecimiento canal", "viral"),
                hashtags = listOf("#Shorts", "#YouTubeTips", "#ViralShorts"),
                durationFormatted = "0:58",
                durationSeconds = 58,
                views = 482100,
                likes = 34500,
                dislikes = 180,
                commentsCount = 1420,
                sharesCount = 5900,
                impressions = 2900000,
                ctrPercent = 14.2,
                avgViewDurationFormatted = "0:56",
                retentionPercent = 96.5,
                publishDate = "Hace 5 días",
                visibility = VisibilityType.PUBLIC,
                videoType = VideoType.SHORT,
                category = "Educación",
                isMonetized = true,
                estimatedRevenueUsd = 128.80,
                swipeAwayPercent = 18.2
            ),
            Video(
                id = "v_103",
                title = "Construí una app completa en 1 hora con Gemini 3.8 Flash",
                description = "Prueba extrema: desde la idea hasta el APK instalado en Android utilizando Jetpack Compose y Firebase.",
                tags = listOf("gemini", "android", "jetpack compose", "desarrollo app", "firebase"),
                hashtags = listOf("#Gemini", "#AndroidDev", "#GoogleAI"),
                durationFormatted = "14:15",
                durationSeconds = 855,
                views = 68900,
                likes = 5400,
                dislikes = 28,
                commentsCount = 380,
                sharesCount = 640,
                impressions = 780000,
                ctrPercent = 8.2,
                avgViewDurationFormatted = "7m 30s",
                retentionPercent = 52.6,
                publishDate = "Hace 1 semana",
                visibility = VisibilityType.PUBLIC,
                videoType = VideoType.REGULAR,
                category = "Ciencia y Tecnología",
                isMonetized = true,
                estimatedRevenueUsd = 210.40
            ),
            Video(
                id = "v_104",
                title = "El secreto del algoritmo de Shorts en 30 segundos",
                description = "Lo único que le importa a YouTube para recomendar tu contenido vertical.",
                tags = listOf("algoritmo", "shorts", "consejos", "creadores"),
                hashtags = listOf("#Shorts", "#Tips"),
                durationFormatted = "0:32",
                durationSeconds = 32,
                views = 310400,
                likes = 21900,
                dislikes = 95,
                commentsCount = 890,
                sharesCount = 3400,
                impressions = 1850000,
                ctrPercent = 16.5,
                avgViewDurationFormatted = "0:34",
                retentionPercent = 106.2,
                publishDate = "Hace 2 semanas",
                visibility = VisibilityType.PUBLIC,
                videoType = VideoType.SHORT,
                category = "Educación",
                isMonetized = true,
                estimatedRevenueUsd = 94.20,
                swipeAwayPercent = 14.0
            ),
            Video(
                id = "v_105",
                title = "Mi Setup de Grabación y Audio Minimalista (Video Privado Sponsors)",
                description = "Recorrido detallado de cámara Sony, micrófono Shure, iluminación y acústica para patrocinadores.",
                tags = listOf("setup", "estudio", "camaras", "microfonos"),
                hashtags = listOf("#Setup", "#Studio"),
                durationFormatted = "22:10",
                durationSeconds = 1330,
                views = 1200,
                likes = 140,
                dislikes = 2,
                commentsCount = 35,
                sharesCount = 12,
                impressions = 5400,
                ctrPercent = 9.1,
                avgViewDurationFormatted = "12m 40s",
                retentionPercent = 57.3,
                publishDate = "Hace 3 semanas",
                visibility = VisibilityType.UNLISTED,
                videoType = VideoType.REGULAR,
                category = "Gente y blogs",
                isMonetized = false,
                estimatedRevenueUsd = 0.0
            ),
            Video(
                id = "v_106",
                title = "Borrador: Cómo monetizar tu canal en menos de 90 días",
                description = "Estrategias de nicho y formato para alcanzar las 4000 horas y 1000 suscriptores.",
                tags = listOf("monetizacion", "ypp", "ganar dinero youtube"),
                hashtags = listOf("#Monetizacion", "#YouTube"),
                durationFormatted = "11:20",
                durationSeconds = 680,
                views = 0,
                likes = 0,
                dislikes = 0,
                commentsCount = 0,
                sharesCount = 0,
                impressions = 0,
                ctrPercent = 0.0,
                avgViewDurationFormatted = "0:00",
                retentionPercent = 0.0,
                publishDate = "Borrador",
                visibility = VisibilityType.PRIVATE,
                videoType = VideoType.REGULAR,
                category = "Educación",
                uploadStatus = UploadStatus.DRAFT,
                isMonetized = true,
                estimatedRevenueUsd = 0.0
            )
        )
    }

    private fun createInitialComments(): List<Comment> {
        return listOf(
            Comment(
                id = "comm_1",
                videoId = "v_101",
                videoTitle = "Cómo programar con IA en 2026: Guía completa paso a paso",
                authorName = "Carlos Developer",
                text = "¿Recomiendas empezar con Python o TypeScript para construir agentes autónomos conectados a APIs?",
                timestamp = "Hace 20 min",
                likesCount = 14,
                hasCreatorHeart = true,
                isAnswered = true,
                isFlaggedForReview = false,
                isSpamOrOffensive = false,
                isImportantQuestion = true,
                replies = listOf(
                    CommentReply(
                        id = "rep_1",
                        authorName = "Barto Tech & AI",
                        text = "¡Excelente pregunta Carlos! Para prototipado y librerías IA oficiales Python sigue siendo el rey, pero para producción e interfaces web/móvil TypeScript es imbatible.",
                        timestamp = "Hace 10 min",
                        isCreatorReply = true
                    )
                )
            ),
            Comment(
                id = "comm_2",
                videoId = "v_102",
                videoTitle = "3 Trucos de YouTube Shorts que el 99% desconoce ⚡",
                authorName = "Mariana Crea",
                text = "¡Apliqué el truco del bucle sonoro en mi último Short y pasó de 800 vistas a 45,000 en 48 horas! Gracias Barto 🎉",
                timestamp = "Hace 1 hora",
                likesCount = 52,
                hasCreatorHeart = false,
                isAnswered = false,
                isFlaggedForReview = false,
                isSpamOrOffensive = false,
                isImportantQuestion = false
            ),
            Comment(
                id = "comm_3",
                videoId = "v_103",
                videoTitle = "Construí una app completa en 1 hora con Gemini 3.8 Flash",
                authorName = "Andrés Tech",
                text = "¿Cuál fue el costo estimado en tokens para generar todo el código de la app?",
                timestamp = "Hace 3 horas",
                likesCount = 8,
                hasCreatorHeart = false,
                isAnswered = false,
                isFlaggedForReview = false,
                isSpamOrOffensive = false,
                isImportantQuestion = true
            ),
            Comment(
                id = "comm_4",
                videoId = "v_101",
                videoTitle = "Cómo programar con IA en 2026: Guía completa paso a paso",
                authorName = "crypto_bot_99",
                text = "CLAIM YOUR 1.5 BTC AIRDROP NOW CLICK HERE bit.ly/free-crypto-giveaway-winner",
                timestamp = "Hace 4 horas",
                likesCount = 0,
                hasCreatorHeart = false,
                isAnswered = false,
                isFlaggedForReview = true,
                isSpamOrOffensive = true,
                isImportantQuestion = false
            ),
            Comment(
                id = "comm_5",
                videoId = "v_102",
                videoTitle = "3 Trucos de YouTube Shorts que el 99% desconoce ⚡",
                authorName = "Lucía Morales",
                text = "El video es súper claro, pero ¿a qué hora exacta recomiendas publicar para audiencia de Latinoamérica?",
                timestamp = "Hace 6 horas",
                likesCount = 19,
                hasCreatorHeart = true,
                isAnswered = false,
                isFlaggedForReview = false,
                isSpamOrOffensive = false,
                isImportantQuestion = true
            )
        )
    }

    private fun createInitialPlaylists(): List<Playlist> {
        return listOf(
            Playlist(
                id = "pl_1",
                title = "Curso Completo: Programación con IA 2026",
                description = "Todos los episodios del curso práctico para desarrolladores y creadores.",
                visibility = VisibilityType.PUBLIC,
                videoIds = listOf("v_101", "v_103"),
                updatedAt = "Hace 3 días"
            ),
            Playlist(
                id = "pl_2",
                title = "Secretos de YouTube Shorts y Crecimiento Viral",
                description = "Guías y experimentos para multiplicar retención y suscriptores.",
                visibility = VisibilityType.PUBLIC,
                videoIds = listOf("v_102", "v_104"),
                updatedAt = "Hace 5 días"
            ),
            Playlist(
                id = "pl_3",
                title = "Recursos Exclusivos y Detrás de Escena",
                description = "Videos especiales y tutoriales en profundidad para miembros.",
                visibility = VisibilityType.UNLISTED,
                videoIds = listOf("v_105"),
                updatedAt = "Hace 2 semanas"
            )
        )
    }

    private fun createInitialScheduled(): List<ScheduledPost> {
        return listOf(
            ScheduledPost(
                id = "sp_1",
                title = "Cómo crear miniaturas irresistibles con IA (Tutorial Práctico)",
                videoType = VideoType.REGULAR,
                scheduledDate = "31/10/2026",
                scheduledTime = "18:00",
                visibility = VisibilityType.PUBLIC
            ),
            ScheduledPost(
                id = "sp_2",
                title = "5 extensiones secretas para creadores de YouTube",
                videoType = VideoType.SHORT,
                scheduledDate = "02/11/2026",
                scheduledTime = "20:00",
                visibility = VisibilityType.PUBLIC
            ),
            ScheduledPost(
                id = "sp_3",
                title = "Comparativa: Gemini vs Claude para escribir guiones de video",
                videoType = VideoType.REGULAR,
                scheduledDate = "05/11/2026",
                scheduledTime = "17:30",
                visibility = VisibilityType.PUBLIC
            )
        )
    }

    private fun createInitialNotifications(): List<AppNotification> {
        return listOf(
            AppNotification(
                id = "n_1",
                title = "¡Hito alcanzado: 140,000 suscriptores!",
                message = "Tu canal 'Barto Tech & AI' superó una nueva meta. ¡Felicitaciones!",
                timestamp = "Hace 2 horas",
                type = NotificationType.SUBSCRIBER_MILESTONE
            ),
            AppNotification(
                id = "n_2",
                title = "Pico viral detectado en Short",
                message = "El video '3 Trucos de YouTube Shorts' aumentó un +320% en reproducciones en la última hora.",
                timestamp = "Hace 5 horas",
                type = NotificationType.VIRAL_SURGE,
                relatedVideoId = "v_102"
            ),
            AppNotification(
                id = "n_3",
                title = "Nuevo comentario prioritario",
                message = "Carlos Developer hizo una pregunta relevante en 'Cómo programar con IA'.",
                timestamp = "Hace 6 horas",
                type = NotificationType.NEW_COMMENT,
                relatedVideoId = "v_101"
            ),
            AppNotification(
                id = "n_4",
                title = "Resumen diario de Barto AI",
                message = "Ayer ganaste +420 suscriptores y 32,800 vistas. Revisa el informe completo.",
                timestamp = "Ayer",
                type = NotificationType.DAILY_SUMMARY
            )
        )
    }

    private fun createInitialCompetitors(): List<CompetitorChannel> {
        return listOf(
            CompetitorChannel(
                id = "comp_1",
                name = "AI Masters Studio",
                handle = "@aimasters",
                subscribersCount = 280000,
                videosCount = 310,
                avgViewsPerVideo = 45000,
                uploadFrequencyWeekly = 3.0,
                topKeywords = listOf("IA", "Automatización", "Productividad", "Python"),
                latestVideoTitle = "La IA revolucionará tu canal de YouTube hoy"
            ),
            CompetitorChannel(
                id = "comp_2",
                name = "Creadores Pro Lab",
                handle = "@creadorespro",
                subscribersCount = 195000,
                videosCount = 240,
                avgViewsPerVideo = 32000,
                uploadFrequencyWeekly = 2.0,
                topKeywords = listOf("Miniaturas", "Shorts", "Algoritmo", "Monetización"),
                latestVideoTitle = "Guía completa para viralizar videos en 2026"
            )
        )
    }

    private fun createInitialAuditLogs(): List<AuditLogEntry> {
        return listOf(
            AuditLogEntry("a_1", "10:30 - Hoy", "Inicio de sesión seguro", "Sesión iniciada con Google OAuth", "Barto Tech & AI"),
            AuditLogEntry("a_2", "11:15 - Hoy", "Comentario respondido", "Respuesta enviada a Carlos Developer", "Barto Tech & AI"),
            AuditLogEntry("a_3", "12:00 - Ayer", "Publicación programada", "Video programado para el 31/10/2026", "Barto Tech & AI")
        )
    }
}
