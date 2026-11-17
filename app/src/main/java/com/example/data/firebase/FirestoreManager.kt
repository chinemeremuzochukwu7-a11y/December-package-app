package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manages the Firestore client configured with the app's custom database ID.
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
}
