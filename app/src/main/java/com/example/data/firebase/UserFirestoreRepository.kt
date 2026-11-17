package com.example.data.firebase

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.data.subscription.SubscriptionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val credits: Long = 0L,
    val isPro: Boolean = false,
    val isYearly: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
    val lastDailyClaimDate: String? = null,
    val lastResetMonth: String? = null
)

class UserFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreManager.getFirestore(context))

    companion object {
        private const val TAG = "UserFirestoreRepo"
        private const val PREFS_NAME = "holiday_user_prefs"
        private const val KEY_USER_ID = "key_persistent_user_id"
        private val initMutex = Mutex()

        fun getOrCreateUserId(context: Context): String {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var id = prefs.getString(KEY_USER_ID, null)
            if (id.isNullOrBlank()) {
                // Deterministic hardware-bound fingerprint that survives Clear Data / Clear Storage
                id = generatePermanentDeviceFingerprint(appContext)
                prefs.edit().putString(KEY_USER_ID, id).apply()
            }
            return id
        }

        /**
         * Generates a deterministic hardware-bound device signature.
         * Settings.Secure.ANDROID_ID is persistent across app reinstalls and Clear Storage
         * on Android (API 26+), with a deterministic Build + package fallback (never random UUID)
         * so clearing phone storage never generates a fresh device ID or grants extra credits.
         */
        fun generatePermanentDeviceFingerprint(context: Context): String {
            val androidId = try {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            } catch (_: Exception) {
                null
            }

            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                return "dev_" + sha256("holiday_wishes_secure_" + androidId).take(20)
            }

            val pkg = try { context.packageName } catch (_: Exception) { "com.geegroup.holidaywish" }
            val hardwareSig = listOf(
                android.os.Build.BOARD,
                android.os.Build.BRAND,
                android.os.Build.DEVICE,
                android.os.Build.HARDWARE,
                android.os.Build.MANUFACTURER,
                android.os.Build.MODEL,
                android.os.Build.PRODUCT,
                android.os.Build.FINGERPRINT,
                pkg
            ).joinToString("_")
            return "dev_hw_" + sha256("holiday_wishes_hw_" + hardwareSig).take(20)
        }

        private fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private fun nowIsoString(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

    /**
     * Atomically initializes or restores the user profile in Firestore bound to the permanent
     * hardware device ID. If the user cleared app storage, restores their exact remaining
     * credit balance, Pro status, and lastDailyClaimDate without granting any new starter credits.
     */
    suspend fun ensureUserInitialized(
        userId: String,
        displayName: String = "Holiday Friend",
        email: String = ""
    ): UserProfile = initMutex.withLock {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        val nowString = nowIsoString()
        val userDoc = db.collection("users").document(userId)
        val claimedDoc = db.collection("claimed_devices").document(userId)

        val profile = db.runTransaction { transaction ->
            val userSnap = transaction.get(userDoc)
            val claimSnap = transaction.get(claimedDoc)

            if (userSnap.exists()) {
                val userCredits = userSnap.getLong("credits") ?: 0L
                val userIsPro = userSnap.getBoolean("isPro") ?: false
                val userIsYearly = userSnap.getBoolean("isYearly") ?: false
                val userDaily = userSnap.getString("lastDailyClaimDate")
                val userResetMonth = userSnap.getString("lastResetMonth")

                val claimCredits = if (claimSnap.exists()) claimSnap.getLong("currentCredits") else null
                val claimIsPro = if (claimSnap.exists()) (claimSnap.getBoolean("isPro") ?: false) else false
                val claimIsYearly = if (claimSnap.exists()) (claimSnap.getBoolean("isYearly") ?: false) else false
                val claimDaily = if (claimSnap.exists()) claimSnap.getString("lastDailyClaimDate") else null
                val claimResetMonth = if (claimSnap.exists()) claimSnap.getString("lastResetMonth") else null

                val effectiveIsPro = userIsPro || claimIsPro
                val effectiveIsYearly = userIsYearly || claimIsYearly
                val effectiveCredits = if (!effectiveIsPro && claimCredits != null) {
                    minOf(userCredits, claimCredits).coerceAtLeast(0L)
                } else {
                    maxOf(userCredits, claimCredits ?: 0L).coerceAtLeast(0L)
                }
                val effectiveDaily = userDaily ?: claimDaily
                val effectiveReset = userResetMonth ?: claimResetMonth

                if (claimSnap.exists()) {
                    val claimUpdate = hashMapOf<String, Any>(
                        "deviceId" to userId,
                        "currentCredits" to effectiveCredits,
                        "isPro" to effectiveIsPro,
                        "isYearly" to effectiveIsYearly,
                        "lastSyncedAt" to nowString
                    )
                    if (!effectiveDaily.isNullOrBlank()) {
                        claimUpdate["lastDailyClaimDate"] = effectiveDaily
                    }
                    if (!effectiveReset.isNullOrBlank()) {
                        claimUpdate["lastResetMonth"] = effectiveReset
                    }
                    transaction.update(claimedDoc, claimUpdate)
                }

                if (effectiveCredits != userCredits || effectiveIsPro != userIsPro || effectiveIsYearly != userIsYearly) {
                    val userUpdate = hashMapOf<String, Any>(
                        "credits" to effectiveCredits,
                        "isPro" to effectiveIsPro,
                        "isYearly" to effectiveIsYearly,
                        "updatedAt" to nowString
                    )
                    if (!effectiveDaily.isNullOrBlank()) {
                        userUpdate["lastDailyClaimDate"] = effectiveDaily
                    }
                    transaction.update(userDoc, userUpdate)
                }

                UserProfile(
                    userId = userId,
                    email = userSnap.getString("email") ?: email,
                    displayName = userSnap.getString("displayName") ?: displayName,
                    credits = effectiveCredits,
                    isPro = effectiveIsPro,
                    isYearly = effectiveIsYearly,
                    createdAt = userSnap.getString("createdAt") ?: nowString,
                    updatedAt = userSnap.getString("updatedAt") ?: nowString,
                    lastDailyClaimDate = effectiveDaily,
                    lastResetMonth = effectiveReset
                )
            } else if (claimSnap.exists()) {
                // User doc missing while device ledger exists: restore exact recorded balance (no +25 reset)
                val restoredCredits = (claimSnap.getLong("currentCredits") ?: 0L).coerceAtLeast(0L)
                val restoredIsPro = claimSnap.getBoolean("isPro") ?: false
                val restoredIsYearly = claimSnap.getBoolean("isYearly") ?: false
                val restoredDaily = claimSnap.getString("lastDailyClaimDate")
                val restoredReset = claimSnap.getString("lastResetMonth")

                val restoredData = hashMapOf<String, Any>(
                    "userId" to userId,
                    "email" to email,
                    "displayName" to displayName,
                    "credits" to restoredCredits,
                    "isPro" to restoredIsPro,
                    "isYearly" to restoredIsYearly,
                    "starterCreditClaimed" to true,
                    "createdAt" to (claimSnap.getString("claimedAt") ?: nowString),
                    "updatedAt" to nowString
                )
                if (!restoredDaily.isNullOrBlank()) {
                    restoredData["lastDailyClaimDate"] = restoredDaily
                }
                if (!restoredReset.isNullOrBlank()) {
                    restoredData["lastResetMonth"] = restoredReset
                }

                transaction.set(userDoc, restoredData)
                transaction.update(
                    claimedDoc,
                    mapOf(
                        "deviceId" to userId,
                        "lastSyncedAt" to nowString
                    )
                )

                UserProfile(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    credits = restoredCredits,
                    isPro = restoredIsPro,
                    isYearly = restoredIsYearly,
                    createdAt = claimSnap.getString("claimedAt") ?: nowString,
                    updatedAt = nowString,
                    lastDailyClaimDate = restoredDaily,
                    lastResetMonth = restoredReset
                )
            } else {
                // Brand new hardware device: atomically grant 25 starter credits once in both collections
                val claimRecord = hashMapOf<String, Any>(
                    "deviceId" to userId,
                    "claimedAt" to nowString,
                    "initialCreditsGranted" to 25L,
                    "currentCredits" to 25L,
                    "isPro" to false,
                    "isYearly" to false,
                    "lastSyncedAt" to nowString
                )
                val initialData = hashMapOf<String, Any>(
                    "userId" to userId,
                    "email" to email,
                    "displayName" to displayName,
                    "credits" to 25L,
                    "isPro" to false,
                    "isYearly" to false,
                    "starterCreditClaimed" to true,
                    "createdAt" to nowString,
                    "updatedAt" to nowString
                )

                transaction.set(claimedDoc, claimRecord)
                transaction.set(userDoc, initialData)

                UserProfile(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    credits = 25L,
                    isPro = false,
                    isYearly = false,
                    createdAt = nowString,
                    updatedAt = nowString,
                    lastDailyClaimDate = null,
                    lastResetMonth = null
                )
            }
        }.await()

        SubscriptionManager.syncWithFirestore(
            firestoreCredits = profile.credits.toInt(),
            firestoreIsPro = profile.isPro,
            isYearly = profile.isYearly,
            lastDailyClaimDate = profile.lastDailyClaimDate,
            lastResetMonth = profile.lastResetMonth
        )
        profile
    }

    /**
     * Observes the user profile in real time from Firestore and keeps SubscriptionManager in sync.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error listening to user profile", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val credits = (snapshot.getLong("credits") ?: 0L).coerceAtLeast(0L)
                val isPro = snapshot.getBoolean("isPro") ?: false
                val isYearly = snapshot.getBoolean("isYearly") ?: false
                val lastDailyClaim = snapshot.getString("lastDailyClaimDate")
                val lastReset = snapshot.getString("lastResetMonth")
                val profile = UserProfile(
                    userId = userId,
                    email = snapshot.getString("email") ?: "",
                    displayName = snapshot.getString("displayName") ?: "Holiday Friend",
                    credits = credits,
                    isPro = isPro,
                    isYearly = isYearly,
                    createdAt = snapshot.getString("createdAt") ?: "",
                    updatedAt = snapshot.getString("updatedAt") ?: "",
                    lastDailyClaimDate = lastDailyClaim,
                    lastResetMonth = lastReset
                )
                SubscriptionManager.syncWithFirestore(
                    firestoreCredits = credits.toInt(),
                    firestoreIsPro = isPro,
                    isYearly = isYearly,
                    lastDailyClaimDate = lastDailyClaim,
                    lastResetMonth = lastReset
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Atomically claims the daily +1 free credit using a Firestore transaction checking both
     * users/{deviceId} and claimed_devices/{deviceId} so clearing storage cannot reset daily claims.
     * Returns the updated credit balance, or null if already claimed today or uninitialized.
     */
    suspend fun claimDailyCredit(userId: String, todayDate: String): Long? = initMutex.withLock {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        val userDoc = db.collection("users").document(userId)
        val claimedDoc = db.collection("claimed_devices").document(userId)
        val nowString = nowIsoString()

        db.runTransaction { transaction ->
            val userSnap = transaction.get(userDoc)
            val claimSnap = transaction.get(claimedDoc)
            if (!userSnap.exists() && !claimSnap.exists()) {
                return@runTransaction null
            }

            val userLastClaim = userSnap.getString("lastDailyClaimDate")
            val claimLastClaim = if (claimSnap.exists()) claimSnap.getString("lastDailyClaimDate") else null
            if (userLastClaim == todayDate || claimLastClaim == todayDate) {
                return@runTransaction null
            }

            val currentCredits = (userSnap.getLong("credits")
                ?: claimSnap.getLong("currentCredits")
                ?: 0L).coerceAtLeast(0L)
            val newCredits = currentCredits + 1L

            if (userSnap.exists()) {
                transaction.update(
                    userDoc,
                    mapOf(
                        "credits" to newCredits,
                        "lastDailyClaimDate" to todayDate,
                        "updatedAt" to nowString
                    )
                )
            }
            if (claimSnap.exists()) {
                transaction.update(
                    claimedDoc,
                    mapOf(
                        "currentCredits" to newCredits,
                        "lastDailyClaimDate" to todayDate,
                        "lastUpdated" to nowString
                    )
                )
            }
            newCredits
        }.await()
    }

    /**
     * Atomically deducts credits from Firestore with dual-layer ledger sync.
     */
    suspend fun deductCredits(userId: String, amount: Int): Boolean {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        if (amount <= 0) return true
        val docRef = db.collection("users").document(userId)
        val claimedRef = db.collection("claimed_devices").document(userId)
        val now = nowIsoString()
        return try {
            db.runTransaction { transaction ->
                val userSnap = transaction.get(docRef)
                val claimSnap = transaction.get(claimedRef)
                if (!userSnap.exists()) return@runTransaction false

                val isPro = userSnap.getBoolean("isPro") ?: false
                val isYearly = userSnap.getBoolean("isYearly") ?: false
                if (isPro && isYearly) {
                    return@runTransaction true
                }
                val currentCredits = (userSnap.getLong("credits") ?: 0L).coerceAtLeast(0L)
                if (currentCredits >= amount) {
                    val newCredits = currentCredits - amount
                    transaction.update(
                        docRef,
                        mapOf("credits" to newCredits, "updatedAt" to now)
                    )
                    if (claimSnap.exists()) {
                        transaction.update(
                            claimedRef,
                            mapOf("currentCredits" to newCredits, "lastUpdated" to now)
                        )
                    }
                    true
                } else {
                    false
                }
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deduct credits", e)
            false
        }
    }

    /**
     * Atomically adds credits in Firestore (e.g., from rewarded ad) and updates the hardware ledger.
     */
    suspend fun addCredits(userId: String, amount: Int): Boolean {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        if (amount <= 0) return false
        val docRef = db.collection("users").document(userId)
        val claimedRef = db.collection("claimed_devices").document(userId)
        val now = nowIsoString()
        return try {
            db.runTransaction { transaction ->
                val userSnap = transaction.get(docRef)
                val claimSnap = transaction.get(claimedRef)
                if (!userSnap.exists()) return@runTransaction false

                val currentCredits = (userSnap.getLong("credits") ?: 0L).coerceAtLeast(0L)
                val newCredits = currentCredits + amount.toLong()
                transaction.update(
                    docRef,
                    mapOf("credits" to newCredits, "updatedAt" to now)
                )
                if (claimSnap.exists()) {
                    transaction.update(
                        claimedRef,
                        mapOf("currentCredits" to newCredits, "lastUpdated" to now)
                    )
                }
                true
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add credits", e)
            false
        }
    }

    /**
     * Atomically updates Pro subscription status and monthly/initial credits in both ledgers.
     */
    suspend fun updateProStatus(
        userId: String,
        isPro: Boolean,
        isYearly: Boolean? = null,
        credits: Long? = null,
        lastResetMonth: String? = null
    ) {
        require(userId.isNotBlank()) { "Device ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        val claimedRef = db.collection("claimed_devices").document(userId)
        val now = nowIsoString()
        try {
            db.runTransaction { transaction ->
                val userSnap = transaction.get(docRef)
                val claimSnap = transaction.get(claimedRef)
                if (!userSnap.exists()) return@runTransaction

                val userUpdates = hashMapOf<String, Any>(
                    "isPro" to isPro,
                    "updatedAt" to now
                )
                val claimUpdates = hashMapOf<String, Any>(
                    "isPro" to isPro,
                    "lastUpdated" to now
                )
                if (isYearly != null) {
                    userUpdates["isYearly"] = isYearly
                    claimUpdates["isYearly"] = isYearly
                }
                if (credits != null) {
                    val safeCredits = credits.coerceAtLeast(0L)
                    userUpdates["credits"] = safeCredits
                    claimUpdates["currentCredits"] = safeCredits
                }
                if (!lastResetMonth.isNullOrBlank()) {
                    userUpdates["lastResetMonth"] = lastResetMonth
                    claimUpdates["lastResetMonth"] = lastResetMonth
                }

                transaction.update(docRef, userUpdates)
                if (claimSnap.exists()) {
                    transaction.update(claimedRef, claimUpdates)
                }
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update Pro status", e)
        }
    }
}
