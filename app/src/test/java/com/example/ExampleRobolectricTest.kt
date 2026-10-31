package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BartoRepository
import com.example.model.AnalyticsPeriod
import com.example.model.VideoType
import com.example.model.VisibilityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Barto YouTube AI", appName)
    }

    @Test
    fun `test repository initial state and channel switcher`() {
        val repo = BartoRepository()
        assertNotNull(repo.activeChannel.value)
        assertEquals("Barto Tech & AI", repo.activeChannel.value.name)

        // Test channel switch
        repo.switchChannel("c_barto_gaming")
        assertEquals("Barto Gaming Studio", repo.activeChannel.value.name)

        // Test analytics calculation
        val analytics = repo.getAnalytics(AnalyticsPeriod.DAYS_28)
        assertTrue(analytics.totalViews > 0)
        assertTrue(analytics.netSubscribers > 0)
    }

    @Test
    fun `test publishing and comment operations`() {
        val repo = BartoRepository()
        val initialCount = repo.videos.value.size

        repo.publishNewVideo(
            title = "Video Test",
            description = "Desc test",
            tags = listOf("tag1"),
            hashtags = listOf("#test"),
            visibility = VisibilityType.PUBLIC,
            videoType = VideoType.REGULAR,
            category = "Educación"
        )

        assertEquals(initialCount + 1, repo.videos.value.size)

        // Test reply to comment
        val firstComment = repo.comments.value.first()
        repo.replyToComment(firstComment.id, "Respuesta de prueba")
        val updatedComment = repo.comments.value.find { it.id == firstComment.id }
        assertTrue(updatedComment?.isAnswered == true)
        assertTrue(updatedComment?.replies?.isNotEmpty() == true)
    }

    @Test
    fun `test youtube auth manager native android config and in memory security`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authManager = com.example.network.YouTubeAuthManager(context)

        // 1. Verify Android package name, SHA-1 and Client ID constants
        assertEquals("com.aistudio.bartoyoutube.qzwkmp", com.example.network.YouTubeAuthManager.PACKAGE_NAME)
        assertEquals("F7:FF:B9:89:83:91:0A:8D:87:70:E4:CC:42:B5:C3:A5:45:BE:17:7F", com.example.network.YouTubeAuthManager.SHA1_FINGERPRINT)
        assertEquals("363588858437-1b2gba4icmt6ggp7p2d8dhve0cbmtojq.apps.googleusercontent.com", com.example.network.YouTubeAuthManager.ANDROID_CLIENT_ID)

        // 2. Verify YouTube scopes
        val scopeUris = com.example.network.YouTubeAuthManager.YOUTUBE_SCOPES.map { it.scopeUri }
        assertTrue(scopeUris.contains("https://www.googleapis.com/auth/youtube"))
        assertTrue(scopeUris.contains("https://www.googleapis.com/auth/youtube.readonly"))
        assertTrue(scopeUris.contains("https://www.googleapis.com/auth/youtube.upload"))
        assertTrue(scopeUris.contains("https://www.googleapis.com/auth/yt-analytics.readonly"))

        // 3. Verify in-memory token lifecycle & no plaintext in SharedPreferences
        val testToken = "ya29.test_secure_memory_token"
        authManager.saveTokens(testToken, expiresInSeconds = 3600)
        assertTrue(authManager.hasValidToken())
        assertEquals(testToken, authManager.getAccessToken())

        // Ensure SharedPreferences does NOT store the secret token in plaintext
        val prefs = context.getSharedPreferences("barto_youtube_auth_prefs", Context.MODE_PRIVATE)
        val allPrefsValues = prefs.all.values.map { it.toString() }
        assertTrue("SharedPreferences must not contain plaintext token", !allPrefsValues.contains(testToken))

        // 4. Verify clear tokens
        authManager.clearTokens()
        assertTrue(!authManager.hasValidToken())
        assertEquals(null, authManager.getAccessToken())
    }
}
