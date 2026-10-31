package com.example.model

enum class PlatformType(val displayName: String, val isAvailable: Boolean) {
    YOUTUBE("YouTube", true),
    TIKTOK("TikTok", false),
    INSTAGRAM("Instagram", false),
    FACEBOOK("Facebook Pages", false),
    X_TWITTER("X / Twitter", false),
    THREADS("Threads", false),
    LINKEDIN("LinkedIn", false)
}

enum class VisibilityType(val displayName: String) {
    PUBLIC("Público"),
    UNLISTED("No listado"),
    PRIVATE("Privado"),
    SCHEDULED("Programado")
}

enum class VideoType(val displayName: String) {
    REGULAR("Video largo"),
    SHORT("YouTube Short")
}

enum class UploadStatus(val displayName: String) {
    PUBLISHED("Publicado"),
    PROCESSING("Procesando en HD"),
    UPLOADING("Subiendo al servidor"),
    DRAFT("Borrador local")
}

data class Channel(
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscribersCount: Int,
    val totalVideos: Int,
    val totalViews: Long,
    val isMonetized: Boolean,
    val watchHoursLastYear: Double,
    val platform: PlatformType = PlatformType.YOUTUBE,
    val links: List<Pair<String, String>> = emptyList(),
    val connectionStatus: String = "Conectado vía Google OAuth",
    val communityGuidelinesViolations: Int = 0
)

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val hashtags: List<String>,
    val durationFormatted: String,
    val durationSeconds: Int,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val commentsCount: Int,
    val sharesCount: Int,
    val impressions: Long,
    val ctrPercent: Double,
    val avgViewDurationFormatted: String,
    val retentionPercent: Double,
    val publishDate: String,
    val visibility: VisibilityType,
    val videoType: VideoType,
    val category: String,
    val commentsEnabled: Boolean = true,
    val isMonetized: Boolean = true,
    val estimatedRevenueUsd: Double = 0.0,
    val uploadStatus: UploadStatus = UploadStatus.PUBLISHED,
    val scheduledPublishTime: String? = null,
    val swipeAwayPercent: Double? = null // for Shorts
)

data class CommentReply(
    val id: String,
    val authorName: String,
    val text: String,
    val timestamp: String,
    val isCreatorReply: Boolean = true
)

data class Comment(
    val id: String,
    val videoId: String,
    val videoTitle: String,
    val authorName: String,
    val text: String,
    val timestamp: String,
    val likesCount: Int,
    val hasCreatorHeart: Boolean = false,
    val isAnswered: Boolean = false,
    val isFlaggedForReview: Boolean = false,
    val isSpamOrOffensive: Boolean = false,
    val isImportantQuestion: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val visibility: VisibilityType,
    val videoIds: List<String>,
    val updatedAt: String
)

enum class AnalyticsPeriod(val label: String, val days: Int) {
    DAYS_7("7 días", 7),
    DAYS_28("28 días", 28),
    DAYS_90("90 días", 90),
    DAYS_365("365 días", 365)
}

data class MetricTrend(
    val value: String,
    val growthPercent: Double,
    val isPositive: Boolean
)

data class ChannelAnalytics(
    val period: AnalyticsPeriod,
    val totalViews: Long,
    val viewsTrend: MetricTrend,
    val watchHours: Double,
    val watchHoursTrend: MetricTrend,
    val currentSubscribers: Int,
    val subscribersGained: Int,
    val subscribersLost: Int,
    val netSubscribers: Int,
    val impressions: Long,
    val ctrPercent: Double,
    val avgViewDuration: String,
    val estimatedRevenueUsd: Double,
    val likes: Long,
    val shares: Long,
    val comments: Long,
    val topSubscribersVideos: List<Pair<String, Int>>,
    val ageDemographics: List<Pair<String, Double>>,
    val topCountries: List<Pair<String, Double>>,
    val trafficSources: List<Pair<String, Double>>,
    val dailyViews: List<Pair<String, Long>>
)

data class ScheduledPost(
    val id: String,
    val title: String,
    val videoType: VideoType,
    val scheduledDate: String,
    val scheduledTime: String,
    val visibility: VisibilityType,
    val isPublished: Boolean = false
)

enum class NotificationType(val title: String) {
    UPLOAD_FINISHED("Publicación completada"),
    VIRAL_SURGE("Pico de crecimiento"),
    NEW_COMMENT("Nuevo comentario prioritario"),
    SUBSCRIBER_MILESTONE("Hito de suscriptores"),
    PUBLISH_ERROR("Alerta de publicación"),
    DAILY_SUMMARY("Resumen diario"),
    WEEKLY_REPORT("Informe semanal IA"),
    ATTENTION_REQUIRED("Comentario requiere atención")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val relatedVideoId: String? = null
)

data class CompetitorChannel(
    val id: String,
    val name: String,
    val handle: String,
    val subscribersCount: Int,
    val videosCount: Int,
    val avgViewsPerVideo: Long,
    val uploadFrequencyWeekly: Double,
    val topKeywords: List<String>,
    val latestVideoTitle: String
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val action: String,
    val details: String,
    val channelName: String
)

data class SyncSummaryResult(
    val channelId: String,
    val channelName: String,
    val subscribers: Int,
    val videoCount: Int,
    val viewCount: Long,
    val playlistsCount: Int,
    val commentsCount: Int
)
