package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirestoreCardsRepository
import com.example.data.firebase.UserFirestoreRepository
import com.example.model.CardDecorationStyle
import com.example.model.CustomCardData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HolidayRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun userProfile_authenticatedOwner_createsAndRestoresProfile() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val userRepo = UserFirestoreRepository(firestore)

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
      userRepo.ensureUserInitialized(userId = uid, displayName = "Alice", email = ALICE_EMAIL)
    }

    assertNotNull(profile)
    assertEquals(uid, profile.userId)
    assertTrue(profile.credits >= 25L)
  }

  @Test
  fun savedCards_authenticatedOwner_canSaveAndObserveCards() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
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
      cardsRepo.saveCard(userId = uid, card = customCard)
    }
    assertTrue(cardId.isNotEmpty())

    val cards = withTimeout(FLOW_TIMEOUT_MS) {
      cardsRepo.observeSavedCards(uid).first { it.isNotEmpty() }
    }
    assertTrue(cards.any { it.id == cardId && it.recipientName == "Mom" })
  }

  @Test
  fun savedCards_crossUserAccess_isPreventedByRules() = runBlocking {
    signInTestUser(BOB_EMAIL)
    val bobCardsRepo = FirestoreCardsRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobCardsRepo.observeSavedCards("other_user_alice_id").first()
      }
      fail("Expected exception when Bob reads another user's cards")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      }
    }
  }

  @Test
  fun savedCards_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = FirestoreCardsRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeSavedCards("random_unauth_user_id").first()
      }
      fail("Expected exception for unauthenticated read")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      }
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@holidaytest.com"
    const val BOB_EMAIL = "bob@holidaytest.com"
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
