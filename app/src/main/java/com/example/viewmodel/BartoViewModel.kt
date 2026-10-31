package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiStudioService
import com.example.data.BartoRepository
import com.example.model.*
import com.example.network.YouTubeNetworkClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class BartoScreen(val title: String) {
    DASHBOARD("Panel de Control"),
    VIDEOS("Videos"),
    SHORTS("Shorts Studio"),
    INBOX("Bandeja Inteligente"),
    ANALYTICS("Analíticas"),
    AI_STUDIO("Estudio de IA"),
    PLAYLISTS("Listas de Reproducción"),
    CALENDAR("Calendario"),
    COMPETITORS("Canales de Referencia"),
    SETTINGS("Ajustes y Canales"),
    NOTIFICATIONS("Notificaciones"),
    PUBLISH_VIDEO("Publicar Video")
}

data class UploadProgressState(
    val isUploading: Boolean = false,
    val progressPercent: Int = 0,
    val currentPhase: String = "",
    val isComplete: Boolean = false
)

data class ConfirmDialogState(
    val isOpen: Boolean = false,
    val title: String = "",
    val message: String = "",
    val confirmButtonText: String = "Confirmar",
    val onConfirm: () -> Unit = {}
)

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val result: SyncSummaryResult) : SyncState()
    data class Error(val message: String) : SyncState()
}

class BartoViewModel @JvmOverloads constructor(
    application: Application,
    val networkClient: YouTubeNetworkClient = YouTubeNetworkClient(application),
    private val repository: BartoRepository = BartoRepository(networkClient),
    private val aiService: GeminiStudioService = GeminiStudioService()
) : AndroidViewModel(application) {

    // YouTube API Live Sync State
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(BartoScreen.DASHBOARD)
    val currentScreen: StateFlow<BartoScreen> = _currentScreen.asStateFlow()

    // Screen Backstack for BackHandler
    private val screenStack = mutableListOf(BartoScreen.DASHBOARD)

    // Repository flows
    val activeChannel: StateFlow<Channel> = repository.activeChannel
    val availableChannels: StateFlow<List<Channel>> = repository.availableChannels
    val connectedPlatforms = repository.connectedPlatforms
    val videos: StateFlow<List<Video>> = repository.videos
    val comments: StateFlow<List<Comment>> = repository.comments
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val scheduledPosts: StateFlow<List<ScheduledPost>> = repository.scheduledPosts
    val notifications: StateFlow<List<AppNotification>> = repository.notifications
    val competitors: StateFlow<List<CompetitorChannel>> = repository.competitors
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
    val hiddenUsers: StateFlow<List<String>> = repository.hiddenUsers

    // Selected Video for details / diagnosis
    private val _selectedVideo = MutableStateFlow<Video?>(null)
    val selectedVideo: StateFlow<Video?> = _selectedVideo.asStateFlow()

    // Analytics Period
    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.DAYS_28)
    val analyticsPeriod: StateFlow<AnalyticsPeriod> = _analyticsPeriod.asStateFlow()

    val analytics: StateFlow<ChannelAnalytics> = _analyticsPeriod.map { period ->
        repository.getAnalytics(period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getAnalytics(AnalyticsPeriod.DAYS_28))

    // Filtering & Search
    private val _videoSearchQuery = MutableStateFlow("")
    val videoSearchQuery = _videoSearchQuery.asStateFlow()

    private val _videoFilterVisibility = MutableStateFlow<VisibilityType?>(null)
    val videoFilterVisibility = _videoFilterVisibility.asStateFlow()

    private val _commentSearchQuery = MutableStateFlow("")
    val commentSearchQuery = _commentSearchQuery.asStateFlow()

    private val _commentFilterPendingOnly = MutableStateFlow(false)
    val commentFilterPendingOnly = _commentFilterPendingOnly.asStateFlow()

    private val _commentFilterSpamOnly = MutableStateFlow(false)
    val commentFilterSpamOnly = _commentFilterSpamOnly.asStateFlow()

    private val _selectedVideoFilterForComments = MutableStateFlow<String?>(null)
    val selectedVideoFilterForComments = _selectedVideoFilterForComments.asStateFlow()

    // Upload simulation
    private val _uploadProgress = MutableStateFlow(UploadProgressState())
    val uploadProgress: StateFlow<UploadProgressState> = _uploadProgress.asStateFlow()

    // Confirmation dialog
    private val _confirmDialog = MutableStateFlow(ConfirmDialogState())
    val confirmDialog: StateFlow<ConfirmDialogState> = _confirmDialog.asStateFlow()

    // AI Generation States
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiGeneratedTitles = MutableStateFlow<List<String>>(emptyList())
    val aiGeneratedTitles: StateFlow<List<String>> = _aiGeneratedTitles.asStateFlow()

    private val _aiGeneratedDescription = MutableStateFlow("")
    val aiGeneratedDescription: StateFlow<String> = _aiGeneratedDescription.asStateFlow()

    private val _aiGeneratedTags = MutableStateFlow<Pair<List<String>, List<String>>>(Pair(emptyList(), emptyList()))
    val aiGeneratedTags: StateFlow<Pair<List<String>, List<String>>> = _aiGeneratedTags.asStateFlow()

    private val _aiVideoDiagnosis = MutableStateFlow("")
    val aiVideoDiagnosis: StateFlow<String> = _aiVideoDiagnosis.asStateFlow()

    private val _aiVideoIdeas = MutableStateFlow<List<String>>(emptyList())
    val aiVideoIdeas: StateFlow<List<String>> = _aiVideoIdeas.asStateFlow()

    private val _aiCommentSentiment = MutableStateFlow("")
    val aiCommentSentiment: StateFlow<String> = _aiCommentSentiment.asStateFlow()

    private val _aiSuggestedReply = MutableStateFlow("")
    val aiSuggestedReply: StateFlow<String> = _aiSuggestedReply.asStateFlow()

    private val _aiExecutiveReport = MutableStateFlow("")
    val aiExecutiveReport: StateFlow<String> = _aiExecutiveReport.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    // App Preferences
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("es") // "es" or "en"
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Navigation methods
    fun navigateTo(screen: BartoScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        } else if (_currentScreen.value != BartoScreen.DASHBOARD) {
            _currentScreen.value = BartoScreen.DASHBOARD
            screenStack.clear()
            screenStack.add(BartoScreen.DASHBOARD)
            return true
        }
        return false
    }

    fun selectVideo(video: Video?) {
        _selectedVideo.value = video
    }

    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
    }

    fun setVideoSearchQuery(query: String) {
        _videoSearchQuery.value = query
    }

    fun setVideoFilterVisibility(visibility: VisibilityType?) {
        _videoFilterVisibility.value = visibility
    }

    fun setCommentSearchQuery(query: String) {
        _commentSearchQuery.value = query
    }

    fun toggleCommentFilterPending() {
        _commentFilterPendingOnly.value = !_commentFilterPendingOnly.value
    }

    fun toggleCommentFilterSpam() {
        _commentFilterSpamOnly.value = !_commentFilterSpamOnly.value
    }

    fun filterCommentsByVideo(videoId: String?) {
        _selectedVideoFilterForComments.value = videoId
    }

    fun switchChannel(channelId: String) {
        repository.switchChannel(channelId)
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(lang: String) {
        _appLanguage.value = lang
    }

    // Video Actions
    fun updateVideo(
        id: String,
        title: String,
        description: String,
        tags: List<String>,
        visibility: VisibilityType,
        category: String,
        commentsEnabled: Boolean
    ) {
        repository.updateVideo(id, title, description, tags, visibility, category, commentsEnabled)
    }

    fun requestDeleteVideo(video: Video) {
        _confirmDialog.value = ConfirmDialogState(
            isOpen = true,
            title = "Eliminar video permanentemente",
            message = "¿Estás seguro de que deseas eliminar '${video.title}'? Esta acción no se puede deshacer y se perderán las estadísticas acumuladas.",
            confirmButtonText = "Eliminar Video",
            onConfirm = {
                repository.deleteVideo(video.id)
                dismissConfirmDialog()
            }
        )
    }

    fun requestDeletePlaylist(playlist: Playlist) {
        _confirmDialog.value = ConfirmDialogState(
            isOpen = true,
            title = "Eliminar lista de reproducción",
            message = "¿Deseas eliminar '${playlist.title}'? Los videos permanecerán en el canal.",
            confirmButtonText = "Eliminar Lista",
            onConfirm = {
                repository.deletePlaylist(playlist.id)
                dismissConfirmDialog()
            }
        )
    }

    fun requestHideUser(userName: String) {
        _confirmDialog.value = ConfirmDialogState(
            isOpen = true,
            title = "Ocultar usuario del canal",
            message = "Los comentarios de '$userName' ya no serán visibles públicamente en ninguno de tus videos.",
            confirmButtonText = "Ocultar Usuario",
            onConfirm = {
                repository.hideUserFromChannel(userName)
                dismissConfirmDialog()
            }
        )
    }

    fun dismissConfirmDialog() {
        _confirmDialog.value = ConfirmDialogState(isOpen = false)
    }

    // Publishing Flow with animated simulation
    fun publishVideo(
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
        viewModelScope.launch {
            _uploadProgress.value = UploadProgressState(isUploading = true, progressPercent = 15, currentPhase = "Subiendo archivo multimedia (1080p / 60fps)...")
            delay(600)
            _uploadProgress.value = UploadProgressState(isUploading = true, progressPercent = 55, currentPhase = "Procesando versión en alta definición (HD)...")
            delay(600)
            _uploadProgress.value = UploadProgressState(isUploading = true, progressPercent = 85, currentPhase = "Comprobando derechos de autor y monetización...")
            delay(500)
            _uploadProgress.value = UploadProgressState(isUploading = true, progressPercent = 100, currentPhase = "¡Publicación finalizada con éxito!", isComplete = true)
            delay(400)

            repository.publishNewVideo(
                title = title,
                description = description,
                tags = tags,
                hashtags = hashtags,
                visibility = visibility,
                videoType = videoType,
                category = category,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime
            )

            _uploadProgress.value = UploadProgressState(isUploading = false)
            _currentScreen.value = BartoScreen.VIDEOS
        }
    }

    // Comment Actions
    fun replyToComment(commentId: String, replyText: String) {
        if (replyText.isNotBlank()) {
            repository.replyToComment(commentId, replyText)
        }
    }

    fun toggleCreatorHeart(commentId: String) {
        repository.toggleCreatorHeart(commentId)
    }

    fun toggleFlagForReview(commentId: String) {
        repository.toggleFlagForReview(commentId)
    }

    fun deleteComment(commentId: String) {
        repository.deleteComment(commentId)
    }

    // Playlist Actions
    fun createPlaylist(title: String, description: String, visibility: VisibilityType, videoIds: List<String>) {
        repository.createPlaylist(title, description, visibility, videoIds)
    }

    fun updatePlaylist(id: String, title: String, description: String, visibility: VisibilityType, videoIds: List<String>) {
        repository.updatePlaylist(id, title, description, visibility, videoIds)
    }

    // Scheduler Actions
    fun reschedulePost(postId: String, newDate: String, newTime: String) {
        repository.reschedulePost(postId, newDate, newTime)
    }

    fun deleteScheduledPost(postId: String) {
        repository.deleteScheduledPost(postId)
    }

    // Notifications Actions
    fun markNotificationAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun clearAllNotifications() {
        repository.clearAllNotifications()
    }

    // Competitors Actions
    fun addCompetitor(name: String, handle: String) {
        repository.addCompetitor(name, handle)
    }

    fun removeCompetitor(id: String) {
        repository.removeCompetitor(id)
    }

    // ==========================================
    // Gemini AI Studio Actions
    // ==========================================
    fun generateAiTitles(topic: String, category: String = "Tecnología") {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.generateTitles(topic, category)
                result.fold(
                    onSuccess = { _aiGeneratedTitles.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage ?: "Error al generar títulos"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun generateAiDescription(title: String, topic: String, keywords: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.generateDescription(title, topic, keywords)
                result.fold(
                    onSuccess = { _aiGeneratedDescription.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage ?: "Error al generar descripción"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun generateAiTags(title: String, category: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.generateTagsAndHashtags(title, category)
                result.fold(
                    onSuccess = { _aiGeneratedTags.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage ?: "Error al generar etiquetas"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun diagnoseVideoWithAi(video: Video) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.diagnoseVideoPerformance(video)
                result.fold(
                    onSuccess = { _aiVideoDiagnosis.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiVideoDiagnosis.value = "Diagnóstico completado localmente."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun suggestNextVideoIdeas() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val recentCommentTexts = comments.value.map { it.text }
                val result = aiService.suggestNextVideoIdeas(activeChannel.value.name, recentCommentTexts)
                result.fold(
                    onSuccess = { _aiVideoIdeas.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun analyzeCommentsSentiment() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.analyzeCommentsSentiment(comments.value)
                result.fold(
                    onSuccess = { _aiCommentSentiment.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun generateSuggestedReply(commentText: String, videoTitle: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val result = aiService.generateSuggestedReply(commentText, videoTitle)
                result.fold(
                    onSuccess = { _aiSuggestedReply.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun generateWeeklyExecutiveReport() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            try {
                val currentAnalytics = analytics.value
                val result = aiService.generateWeeklyExecutiveReport(
                    activeChannel.value.name,
                    currentAnalytics.totalViews,
                    currentAnalytics.subscribersGained,
                    currentAnalytics.watchHours
                )
                result.fold(
                    onSuccess = { _aiExecutiveReport.value = it },
                    onFailure = { _aiErrorMessage.value = it.localizedMessage }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // YouTube Live Sync API Operations
    fun startNativeAuthorization(
        activity: android.app.Activity,
        onResolutionRequired: (android.app.PendingIntent) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        networkClient.authManager.startNativeAuthorization(activity, onResolutionRequired, onSuccess, onFailure)
    }

    fun handleAuthorizationResult(
        context: android.content.Context,
        data: android.content.Intent?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        networkClient.authManager.handleAuthorizationResult(context, data, onSuccess, onFailure)
    }

    fun hasValidAccessToken(): Boolean {
        return networkClient.authManager.hasValidToken()
    }

    fun syncWithYouTube(manualToken: String? = null) {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            try {
                val result = repository.syncWithYouTubeApi(manualToken)
                result.fold(
                    onSuccess = { summary ->
                        _syncState.value = SyncState.Success(summary)
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(error.message ?: "Error al sincronizar con YouTube Data API")
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Fallo de comunicación de red con YouTube API")
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    fun disconnectYouTubeAccount() {
        networkClient.authManager.clearTokens()
        _syncState.value = SyncState.Idle
    }
}
