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

    companion object {
        private const val PREFS_NAME = "holiday_user_prefs"
        private const val KEY_USER_ID = "key_persistent_user_id"

        fun getOrCreateUserId(context: Context): String {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var id = prefs.getString(KEY_USER_ID, null)
            if (id.isNullOrBlank()) {
                // Generate hardware-bound fingerprint that survives internal storage / clear data
                id = generatePermanentDeviceFingerprint(appContext)
                prefs.edit().putString(KEY_USER_ID, id).apply()
            }
            return id
        }

        /**
         * Generates a permanent hardware-bound device signature.
         * Settings.Secure.ANDROID_ID is persistent across app reinstalls
         * and clear data / clear storage actions on Android.
         */
        private fun generatePermanentDeviceFingerprint(context: Context): String {
            return try {
                val androidId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                    "dev_" + sha256("holiday_wishes_secure_" + androidId).take(20)
                } else {
                    val hardwareSig = "${android.os.Build.BOARD}_${android.os.Build.BRAND}_${android.os.Build.DEVICE}_${android.os.Build.HARDWARE}_${android.os.Build.MANUFACTURER}_${android.os.Build.MODEL}"
                    "dev_hw_" + sha256("holiday_wishes_hw_" + hardwareSig).take(20)
                }
            } catch (e: Exception) {
                "dev_gen_" + java.util.UUID.randomUUID().toString().replace("-", "").take(16)
            }
        }

        private fun sha256(input: String): String {
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Initializes the user document in Firestore with anti-abuse protection.
     * Prevents users from getting free credits repeatedly by clearing app storage.
     */
    suspend fun ensureUserInitialized(
        userId: String,
        displayName: String = "Holiday Friend",
        email: String = ""
    ): UserProfile {
        val nowString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return try {
            val userDoc = db.collection("users").document(userId)
            val claimedDoc = db.collection("claimed_devices").document(userId)

            val snapshot = userDoc.get().await()

            if (!snapshot.exists()) {
                // Anti-Abuse Check: Has this physical device ALREADY claimed starter credits before?
                val claimSnapshot = try { claimedDoc.get().await() } catch (_: Exception) { null }
                val hasAlreadyClaimed = claimSnapshot?.exists() == true

                // If device already claimed previously (e.g. user cleared app data), DO NOT award credits again!
                val starterCredits = if (hasAlreadyClaimed) 0L else 25L

                val initialProfile = UserProfile(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    credits = starterCredits,
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
                    "starterCreditClaimed" to true,
                    "createdAt" to initialProfile.createdAt,
                    "updatedAt" to initialProfile.updatedAt
                )
                userDoc.set(data).await()

                // Mark device in permanent claimed ledger so clear storage can NEVER re-award credits
                if (!hasAlreadyClaimed) {
                    val claimRecord = hashMapOf(
                        "deviceId" to userId,
                        "claimedAt" to nowString,
                        "initialCreditsGranted" to 25L
                    )
                    try { claimedDoc.set(claimRecord).await() } catch (_: Exception) {}
                }

                SubscriptionManager.syncWithFirestore(starterCredits.toInt(), false)
                initialProfile
            } else {
                // Document already exists on Firestore! Restore real current balance.
                val credits = snapshot.getLong("credits") ?: 0L
                val isPro = snapshot.getBoolean("isPro") ?: false
                SubscriptionManager.syncWithFirestore(credits.toInt(), isPro)
                UserProfile(
                    userId = userId,
                    email = snapshot.getString("email") ?: email,
                    displayName = snapshot.getString("displayName") ?: displayName,
                    credits = credits,
                    isPro = isPro,
                    createdAt = snapshot.getString("createdAt") ?: nowString,
                    updatedAt = snapshot.getString("updatedAt") ?: nowString
                )
            }
        } catch (e: Exception) {
            Log.w("UserFirestoreRepo", "Notice initializing user profile: ${e.message}")
            SubscriptionManager.syncWithFirestore(SubscriptionManager.credits.value, SubscriptionManager.isPro.value)
            UserProfile(
                userId = userId,
                email = email,
                displayName = displayName,
                credits = SubscriptionManager.credits.value.toLong(),
                isPro = SubscriptionManager.isPro.value,
                createdAt = nowString,
                updatedAt = nowString
            )
        }
    }

    suspend fun ensureUserInitialized(user: FirebaseUser): UserProfile {
        return ensureUserInitialized(
            userId = user.uid,
            displayName = user.displayName ?: "Holiday Friend",
            email = user.email ?: ""
        )
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
