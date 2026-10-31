package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeChannelListResponse(
    val kind: String?,
    val etag: String?,
    val pageInfo: PageInfo?,
    val items: List<YouTubeChannelItem>?
)

@JsonClass(generateAdapter = true)
data class PageInfo(
    val totalResults: Int?,
    val resultsPerPage: Int?
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelItem(
    val id: String,
    val snippet: ChannelSnippet?,
    val contentDetails: ChannelContentDetails?,
    val statistics: ChannelStatistics?
)

@JsonClass(generateAdapter = true)
data class ChannelSnippet(
    val title: String?,
    val description: String?,
    val customUrl: String?,
    val publishedAt: String?,
    val thumbnails: Thumbnails?,
    val country: String?
)

@JsonClass(generateAdapter = true)
data class Thumbnails(
    val default: ThumbnailItem?,
    val medium: ThumbnailItem?,
    val high: ThumbnailItem?
)

@JsonClass(generateAdapter = true)
data class ThumbnailItem(
    val url: String?,
    val width: Int?,
    val height: Int?
)

@JsonClass(generateAdapter = true)
data class ChannelContentDetails(
    val relatedPlaylists: RelatedPlaylists?
)

@JsonClass(generateAdapter = true)
data class RelatedPlaylists(
    val likes: String?,
    val uploads: String?
)

@JsonClass(generateAdapter = true)
data class ChannelStatistics(
    val viewCount: String?,
    val subscriberCount: String?,
    val hiddenSubscriberCount: Boolean?,
    val videoCount: String?
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemResponse(
    val items: List<PlaylistItem>?
)

@JsonClass(generateAdapter = true)
data class PlaylistItem(
    val id: String,
    val snippet: PlaylistItemSnippet?,
    val contentDetails: PlaylistItemContentDetails?
)

@JsonClass(generateAdapter = true)
data class PlaylistItemSnippet(
    val title: String?,
    val description: String?,
    val publishedAt: String?,
    val resourceId: ResourceId?,
    val thumbnails: Thumbnails?
)

@JsonClass(generateAdapter = true)
data class ResourceId(
    val kind: String?,
    val videoId: String?
)

@JsonClass(generateAdapter = true)
data class PlaylistItemContentDetails(
    val videoId: String?,
    val videoPublishedAt: String?
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    val items: List<YouTubeVideoItem>?
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoItem(
    val id: String,
    val snippet: VideoSnippet?,
    val contentDetails: VideoContentDetails?,
    val statistics: VideoStatistics?
)

@JsonClass(generateAdapter = true)
data class VideoSnippet(
    val title: String?,
    val description: String?,
    val tags: List<String>?,
    val categoryId: String?,
    val thumbnails: Thumbnails?
)

@JsonClass(generateAdapter = true)
data class VideoContentDetails(
    val duration: String?
)

@JsonClass(generateAdapter = true)
data class VideoStatistics(
    val viewCount: String?,
    val likeCount: String?,
    val commentCount: String?
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistResponse(
    val items: List<YouTubePlaylistItemData>?
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemData(
    val id: String,
    val snippet: PlaylistSnippet?,
    val contentDetails: PlaylistContentDetails?
)

@JsonClass(generateAdapter = true)
data class PlaylistSnippet(
    val title: String?,
    val description: String?,
    val publishedAt: String?
)

@JsonClass(generateAdapter = true)
data class PlaylistContentDetails(
    val itemCount: Int?
)

@JsonClass(generateAdapter = true)
data class YouTubeCommentThreadResponse(
    val items: List<CommentThreadItem>?
)

@JsonClass(generateAdapter = true)
data class CommentThreadItem(
    val id: String,
    val snippet: CommentThreadSnippet?
)

@JsonClass(generateAdapter = true)
data class CommentThreadSnippet(
    val videoId: String?,
    val topLevelComment: TopLevelComment?,
    val totalReplyCount: Int?
)

@JsonClass(generateAdapter = true)
data class TopLevelComment(
    val id: String?,
    val snippet: CommentDetailSnippet?
)

@JsonClass(generateAdapter = true)
data class CommentDetailSnippet(
    val authorDisplayName: String?,
    val authorProfileImageUrl: String?,
    val textDisplay: String?,
    val likeCount: Int?,
    val publishedAt: String?
)

@JsonClass(generateAdapter = true)
data class GoogleApiErrorResponse(
    val error: GoogleApiErrorDetails?
)

@JsonClass(generateAdapter = true)
data class GoogleApiErrorDetails(
    val code: Int?,
    val message: String?,
    val errors: List<GoogleApiErrorItem>?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class GoogleApiErrorItem(
    val message: String?,
    val domain: String?,
    val reason: String?
)
