package com.example.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("channels")
    suspend fun getMyChannel(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("mine") mine: Boolean = true
    ): Response<YouTubeChannelListResponse>

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50
    ): Response<YouTubePlaylistItemResponse>

    @GET("videos")
    suspend fun getVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") id: String
    ): Response<YouTubeVideoListResponse>

    @GET("playlists")
    suspend fun getMyPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 25
    ): Response<YouTubePlaylistResponse>

    @GET("commentThreads")
    suspend fun getMyCommentThreads(
        @Query("part") part: String = "snippet,replies",
        @Query("allThreadsRelatedToChannelId") channelId: String,
        @Query("maxResults") maxResults: Int = 50
    ): Response<YouTubeCommentThreadResponse>
}
