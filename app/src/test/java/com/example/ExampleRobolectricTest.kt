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
        assertTrue(allTemplates.size >= 100)

        val christmasTemplates = CardTemplatesRepository.getChristmasTemplates()
        assertTrue(christmasTemplates.size >= 40)
        assertTrue(christmasTemplates.all { it.occasion == CardOccasion.CHRISTMAS })

        val newYearTemplates = CardTemplatesRepository.getNewYearTemplates()
        assertTrue(newYearTemplates.size >= 40)
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

    @Test
    fun `verify RevenueCat configuration and entitlement identifiers`() {
        assertEquals("holiday_wishes_pro", com.example.data.subscription.SubscriptionManager.ENTITLEMENT_HOLIDAY_WISHES_PRO)
        assertEquals("yearly", com.example.data.subscription.SubscriptionManager.PRODUCT_YEARLY)
        assertEquals("monthly", com.example.data.subscription.SubscriptionManager.PRODUCT_MONTHLY)
    }

    @Test
    fun `verify tracker calculations and date helpers`() {
        val today = java.util.Calendar.getInstance()
        val currentMonth = today.get(java.util.Calendar.MONTH) + 1
        val currentDay = today.get(java.util.Calendar.DAY_OF_MONTH)

        // Today's birthday has 0 days until birthday
        val daysToday = com.example.data.repository.TrackersRepository.daysUntilBirthday(currentMonth, currentDay)
        assertEquals(0, daysToday)

        val turningAge = com.example.data.repository.TrackersRepository.calculateTurningAge(2000, currentMonth, currentDay)
        assertNotNull(turningAge)
        assertTrue(turningAge!! >= 24)

        val todayDateString = com.example.data.repository.TrackersRepository.getTodayDateString()
        assertTrue(todayDateString.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }

    @Test
    fun `verify alarm entity and receiver actions`() {
        val alarm = com.example.data.local.AlarmEntity(
            title = "Christmas Morning Wakeup",
            type = "ALARM",
            hour = 7,
            minute = 0,
            repeatMode = "Daily"
        )
        assertEquals("Christmas Morning Wakeup", alarm.title)
        assertEquals("ALARM", alarm.type)
        assertEquals(7, alarm.hour)
        assertEquals(0, alarm.minute)
        assertTrue(alarm.isEnabled)
        assertEquals("com.example.ACTION_ALARM_TRIGGER", com.example.receiver.AlarmReceiver.ACTION_TRIGGER_ALARM)
        assertEquals("com.example.ACTION_DISMISS_ALARM", com.example.receiver.AlarmReceiver.ACTION_DISMISS_ALARM)
    }
}
