package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.subscription.SubscriptionManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val credits: Long = 25L,
    val isPro: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

class UserFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreManager.getFirestore(context))

    private var profileListener: ListenerRegistration? = null

    /**
     * Initializes the user document in Firestore on first sign-in.
     * Every new user receives exactly 25 credits as requested.
     */
    suspend fun ensureUserInitialized(user: FirebaseUser): UserProfile {
        val nowString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return try {
            val userDoc = db.collection("users").document(user.uid)
            val snapshot = userDoc.get().await()

            if (!snapshot.exists()) {
                val initialProfile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Holiday Friend",
                    credits = 25L, // Exactly 25 credits for every new user
                    isPro = false,
                    createdAt = nowString,
                    updatedAt = nowString
                )
                val data = hashMapOf(
                    "userId" to initialProfile.userId,
                    "email" to initialProfile.email,
                    "displayName" to initialProfile.displayName,
                    "credits" to initialProfile.credits,
                    "isPro" to initialProfile.isPro,
                    "createdAt" to initialProfile.createdAt,
                    "updatedAt" to initialProfile.updatedAt
                )
                userDoc.set(data).await()
                SubscriptionManager.syncWithFirestore(25, false)
                initialProfile
            } else {
                val credits = snapshot.getLong("credits") ?: 25L
                val isPro = snapshot.getBoolean("isPro") ?: false
                SubscriptionManager.syncWithFirestore(credits.toInt(), isPro)
                UserProfile(
                    userId = user.uid,
                    email = snapshot.getString("email") ?: (user.email ?: ""),
                    displayName = snapshot.getString("displayName") ?: (user.displayName ?: "Holiday Friend"),
                    credits = credits,
                    isPro = isPro,
                    createdAt = snapshot.getString("createdAt") ?: nowString,
                    updatedAt = snapshot.getString("updatedAt") ?: nowString
                )
            }
        } catch (e: Exception) {
            Log.w("UserFirestoreRepo", "Notice initializing user profile: ${e.message}")
            SubscriptionManager.syncWithFirestore(25, false)
            UserProfile(
                userId = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "Holiday Friend",
                credits = 25L,
                isPro = false,
                createdAt = nowString,
                updatedAt = nowString
            )
        }
    }

    /**
     * Observes the user profile real-time from Firestore and keeps SubscriptionManager in sync.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("UserFirestoreRepo", "Error listening to user profile", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val credits = snapshot.getLong("credits") ?: 25L
                val isPro = snapshot.getBoolean("isPro") ?: false
                val profile = UserProfile(
                    userId = userId,
                    email = snapshot.getString("email") ?: "",
                    displayName = snapshot.getString("displayName") ?: "Holiday Friend",
                    credits = credits,
                    isPro = isPro,
                    createdAt = snapshot.getString("createdAt") ?: "",
                    updatedAt = snapshot.getString("updatedAt") ?: ""
                )
                SubscriptionManager.syncWithFirestore(credits.toInt(), isPro)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Atomically deducts credits from Firestore for text generation or card creation.
     */
    suspend fun deductCredits(userId: String, amount: Int): Boolean {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentCredits = snapshot.getLong("credits") ?: 0L
                val isPro = snapshot.getBoolean("isPro") ?: false
                if (isPro) {
                    // Pro members have unlimited credits
                    return@runTransaction true
                }
                if (currentCredits >= amount) {
                    val newCredits = currentCredits - amount
                    transaction.update(docRef, "credits", newCredits)
                    val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                    transaction.update(docRef, "updatedAt", now)
                    true
                } else {
                    false
                }
            }.await()
        } catch (e: Exception) {
            Log.e("UserFirestoreRepo", "Failed to deduct credits", e)
            false
        }
    }

    /**
     * Adds credits in Firestore (e.g., from rewarded ad or subscription).
     */
    suspend fun addCredits(userId: String, amount: Int) {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                "credits", FieldValue.increment(amount.toLong()),
                "updatedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            ).await()
        } catch (e: Exception) {
            Log.e("UserFirestoreRepo", "Failed to add credits", e)
        }
    }

    /**
     * Updates Pro subscription status in Firestore.
     */
    suspend fun setProStatus(userId: String, isPro: Boolean, creditsBonus: Int = 300) {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                "isPro", isPro,
                "credits", FieldValue.increment(creditsBonus.toLong()),
                "updatedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            ).await()
        } catch (e: Exception) {
            Log.e("UserFirestoreRepo", "Failed to update pro status", e)
        }
    }
}
