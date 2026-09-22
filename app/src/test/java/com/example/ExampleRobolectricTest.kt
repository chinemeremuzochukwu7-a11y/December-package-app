package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.CardTemplatesRepository
import com.example.data.repository.MockAiWishRepository
import com.example.model.AiLanguage
import com.example.model.AiOccasion
import com.example.model.AiRecipient
import com.example.model.AiTone
import com.example.model.AiWishRequest
import com.example.model.CardOccasion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Holiday Wishes", appName)
    }

    @Test
    fun `verify card templates count and occasions`() {
        val allTemplates = CardTemplatesRepository.getAllTemplates()
        assertEquals(20, allTemplates.size)

        val christmasTemplates = CardTemplatesRepository.getChristmasTemplates()
        assertEquals(10, christmasTemplates.size)
        assertTrue(christmasTemplates.all { it.occasion == CardOccasion.CHRISTMAS })

        val newYearTemplates = CardTemplatesRepository.getNewYearTemplates()
        assertEquals(10, newYearTemplates.size)
        assertTrue(newYearTemplates.all { it.occasion == CardOccasion.NEW_YEAR })
    }

    @Test
    fun `verify AI wish generator architecture generates offline personalized response`() = runBlocking {
        val repo = MockAiWishRepository()
        val request = AiWishRequest(
            occasion = AiOccasion.CHRISTMAS,
            recipient = AiRecipient.MOTHER,
            recipientName = "Mary",
            tone = AiTone.WARM,
            language = AiLanguage.ENGLISH,
            personalDetails = "Thank you for all your love and care throughout the year."
        )

        val result = repo.generateWish(request)
        assertTrue(result.isSuccess)

        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(AiOccasion.CHRISTMAS, response!!.occasion)
        assertEquals(AiRecipient.MOTHER, response.recipient)
        assertEquals("Mary", response.recipientName)
        assertEquals(AiTone.WARM, response.tone)
        assertEquals(AiLanguage.ENGLISH, response.language)
        assertTrue(response.text.contains("Mary"))
        assertTrue(response.text.contains("Christmas"))
    }

    @Test
    fun `verify AI wish generator multilingual support`() = runBlocking {
        val repo = MockAiWishRepository()

        // Spanish test
        val esResult = repo.generateWish(
            AiWishRequest(
                occasion = AiOccasion.NEW_YEAR,
                recipient = AiRecipient.FRIEND,
                recipientName = "Carlos",
                tone = AiTone.WARM,
                language = AiLanguage.SPANISH
            )
        )
        assertTrue(esResult.isSuccess)
        assertTrue(esResult.getOrNull()!!.text.contains("Carlos"))

        // French test
        val frResult = repo.generateWish(
            AiWishRequest(
                occasion = AiOccasion.CHRISTMAS,
                recipient = AiRecipient.FAMILY,
                recipientName = "Famille Dupont",
                tone = AiTone.WARM,
                language = AiLanguage.FRENCH
            )
        )
        assertTrue(frResult.isSuccess)
        assertTrue(frResult.getOrNull()!!.text.contains("Famille Dupont"))
    }
}
