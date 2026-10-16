package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Manages the Firestore client configured with the custom database ID
 * and provides reactive Firebase Authentication state.
 */
object FirestoreManager {

    fun getFirestore(context: Context): FirebaseFirestore {
        val databaseId = try {
            context.applicationContext.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            ""
        }
        return if (databaseId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance(databaseId)
            } catch (_: Exception) {
                FirebaseFirestore.getInstance()
            }
        } else {
            FirebaseFirestore.getInstance()
        }
    }

    val auth: FirebaseAuth
        get() = try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            Firebase.auth
        }

    val currentUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (_: Exception) {
            null
        }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        try {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser)
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        } catch (_: Exception) {
            trySend(null)
            awaitClose {}
        }
    }
}
