package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Kisan Wala`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kisan Wala", appName)
    }

    @Test
    fun `placeholder ai repository provides labeled sample responses and agricultural disclaimers`() = runTest {
        val repo = PlaceholderAiRepository()
        assertFalse(repo.isLiveAiConnected)

        val plantRes = repo.analyzePlantLeaf(null, "गेहूँ (Wheat)", "पत्ती पीली हो रही है")
        assertTrue(plantRes.isSamplePreview)
        assertTrue(plantRes.uncertaintyNote.contains("100%"))

        val soilRes = repo.analyzeSoilPhoto(null, "धान", "वाराणसी")
        assertTrue(soilRes.isSamplePreview)
        assertTrue(soilRes.photoLimitationDisclaimer.contains("pH"))
        assertTrue(soilRes.recommendedLabTests.isNotEmpty())

        val fertRes = repo.getFertilizerAdvice("गेहूँ", "वानस्पतिक वृद्धि", "", "")
        assertTrue(fertRes.excessiveUseWarning.contains("यूरिया"))

        val waterRes = repo.getWaterAdvice("टमाटर", "फूल आना", 22f, "गर्म")
        assertTrue(waterRes.shouldWaterNow)

        val chatChunks = repo.streamChatResponse("खाद कब डालें?", AppLanguage.BHOJPURI, "").toList()
        assertTrue(chatChunks.last().contains("किसान भाई"))
    }
}
