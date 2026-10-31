package com.example.network

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "YouTubeNetworkClient"
private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

class YouTubeNetworkClient(context: Context) {

    val authManager = YouTubeAuthManager(context)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authManager.getAccessToken()

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            Log.w(TAG, "YouTube API HTTP response code: ${response.code} for endpoint: ${original.url.encodedPath}")
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: YouTubeApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(YouTubeApiService::class.java)

    fun parseErrorBody(rawErrorJson: String?): String {
        if (rawErrorJson.isNullOrBlank()) return "Error desconocido del servidor de Google."
        return try {
            val adapter = moshi.adapter(GoogleApiErrorResponse::class.java)
            val errorResponse = adapter.fromJson(rawErrorJson)
            val details = errorResponse?.error
            if (details != null) {
                "${details.message ?: "Error en la petición"} (Código ${details.code ?: "?"})"
            } else {
                rawErrorJson
            }
        } catch (e: Exception) {
            rawErrorJson
        }
    }
}
