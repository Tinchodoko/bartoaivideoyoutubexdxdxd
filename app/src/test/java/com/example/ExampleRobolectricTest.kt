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
}
