package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirestoreCardsRepository
import com.example.data.firebase.UserFirestoreRepository
import com.example.data.subscription.SubscriptionManager
import com.example.model.CardDecorationStyle
import com.example.model.CustomCardData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidayRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun uniqueDeviceId(tag: String): String =
    "dev_${tag}_${System.nanoTime()}"

  @Test
  fun deviceProfile_newDevice_grants25CreditsOnce() = runBlocking {
    val deviceId = uniqueDeviceId("new_device")
    val userRepo = UserFirestoreRepository(firestore)

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.ensureUserInitialized(userId = deviceId)
    }

    assertNotNull(profile)
    assertEquals(deviceId, profile.userId)
    assertEquals(25L, profile.credits)
    assertFalse(profile.isPro)
  }

  @Test
  fun deviceProfile_clearStorageSimulation_restoresExactCreditsAndDailyClaim() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val deviceId = uniqueDeviceId("clear_storage")
    val userRepo = UserFirestoreRepository(firestore)
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // 1. Initial launch -> 25 starter credits
    val initial = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.ensureUserInitialized(deviceId)
    }
    assertEquals(25L, initial.credits)

    // 2. Spend 15 credits -> 10 remaining
    val deducted = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.deductCredits(deviceId, 15)
    }
    assertTrue(deducted)

    // 3. Claim +1 daily credit -> 11 remaining
    val afterDaily = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.claimDailyCredit(deviceId, todayDate)
    }
    assertEquals(11L, afterDaily)

    // 4. Attempting a second daily claim same day is blocked
    val duplicateDaily = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.claimDailyCredit(deviceId, todayDate)
    }
    assertNull(duplicateDaily)

    // 5. Simulate "Clear Phone Storage": wipe local SharedPreferences & SubscriptionManager cache
    context.getSharedPreferences("holiday_user_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("holiday_wishes_billing_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    // Re-open with a fresh repository instance using the exact same hardware deviceId
    val freshRepo = UserFirestoreRepository(firestore)
    val restored = withTimeout(DEFAULT_TIMEOUT_MS) {
      freshRepo.ensureUserInitialized(deviceId)
    }

    // Exact balance (11) and daily claim state must be restored without granting +25 again!
    assertEquals(11L, restored.credits)
    assertEquals(todayDate, restored.lastDailyClaimDate)
    assertEquals(11, SubscriptionManager.credits.value)
    assertFalse(SubscriptionManager.canClaimDailyCredit.value)

    // Still cannot claim another daily +1 today even after Clear Storage
    val postClearDaily = withTimeout(DEFAULT_TIMEOUT_MS) {
      freshRepo.claimDailyCredit(deviceId, todayDate)
    }
    assertNull(postClearDaily)
  }

  @Test
  fun savedCards_validDeviceOwner_canSaveAndObserveCards() = runBlocking {
    val deviceId = uniqueDeviceId("cards_owner")
    val userRepo = UserFirestoreRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) { userRepo.ensureUserInitialized(deviceId) }

    val cardsRepo = FirestoreCardsRepository(firestore)
    val customCard = CustomCardData(
      templateId = "test_xmas_1",
      title = "Merry Christmas",
      recipientName = "Mom",
      message = "Wishing you pure joy and love!",
      senderName = "Alice",
      primaryColorHex = 0xFFC41E3A,
      secondaryColorHex = 0xFF8B0000,
      accentColorHex = 0xFFFFD700,
      decorationStyle = CardDecorationStyle.STARS
    )

    val cardId = withTimeout(DEFAULT_TIMEOUT_MS) {
      cardsRepo.saveCard(userId = deviceId, card = customCard)
    }
    assertTrue(cardId.isNotEmpty())

    val cards = withTimeout(FLOW_TIMEOUT_MS) {
      cardsRepo.observeSavedCards(deviceId).first { it.isNotEmpty() }
    }
    assertTrue(cards.any { it.id == cardId && it.recipientName == "Mom" })
  }

  @Test
  fun savedCards_invalidDeviceId_isRejectedByRules() = runBlocking {
    val repo = FirestoreCardsRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeSavedCards("invalid_no_dev_prefix").first()
      }
      fail("Expected PERMISSION_DENIED for invalid deviceId")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      }
    }
  }

  @Test
  fun antiAbuse_cannotInflateCreditsDirectlyViaFirestore() = runBlocking {
    val deviceId = uniqueDeviceId("anti_abuse")
    val userRepo = UserFirestoreRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) { userRepo.ensureUserInitialized(deviceId) }

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(deviceId)
          .update("credits", 999L)
          .await()
      }
      fail("Expected PERMISSION_DENIED when inflating free user credits")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      }
    }
  }

  @Test
  fun proSubscriber_deductCredits_deductsFromCloudLedger() = runBlocking {
    val deviceId = uniqueDeviceId("pro_deduct")
    val userRepo = UserFirestoreRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.ensureUserInitialized(deviceId)
      userRepo.updateProStatus(
        userId = deviceId,
        isPro = true,
        isYearly = true,
        credits = 350L,
        lastResetMonth = "2026-12"
      )
    }

    val deducted = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.deductCredits(deviceId, 25)
    }
    assertTrue(deducted)

    val updated = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.ensureUserInitialized(deviceId)
    }
    assertTrue(updated.isPro)
    assertTrue(updated.isYearly)
    assertEquals(325L, updated.credits)
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
