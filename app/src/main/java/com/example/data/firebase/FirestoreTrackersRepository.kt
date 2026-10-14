package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirestoreBirthday(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val birthDate: String = "",
    val relationship: String = "",
    val notes: String = "",
    val createdAt: String = ""
)

data class FirestoreExpense(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "General",
    val date: String = "",
    val createdAt: String = ""
)

data class FirestoreMeal(
    val id: String = "",
    val userId: String = "",
    val mealType: String = "Dinner",
    val title: String = "",
    val recipe: String = "",
    val date: String = "",
    val createdAt: String = ""
)

class FirestoreTrackersRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreManager.getFirestore(context))

    // ---------------------------------
    // BIRTHDAYS
    // ---------------------------------
    fun observeBirthdays(userId: String): Flow<List<FirestoreBirthday>> = callbackFlow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val reg = db.collection("users").document(userId).collection("birthdays")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreTrackers", "Error listening to birthdays", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            FirestoreBirthday(
                                id = doc.id,
                                userId = userId,
                                name = doc.getString("name") ?: "",
                                birthDate = doc.getString("birthDate") ?: "",
                                relationship = doc.getString("relationship") ?: "",
                                notes = doc.getString("notes") ?: "",
                                createdAt = doc.getString("createdAt") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveBirthday(userId: String, name: String, birthDate: String, relationship: String, notes: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val id = "bday_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "id" to id,
            "userId" to userId,
            "name" to name,
            "birthDate" to birthDate,
            "relationship" to relationship,
            "notes" to notes,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        db.collection("users").document(userId).collection("birthdays").document(id).set(data).await()
        return id
    }

    suspend fun deleteBirthday(userId: String, id: String) {
        db.collection("users").document(userId).collection("birthdays").document(id).delete().await()
    }

    // ---------------------------------
    // EXPENSES
    // ---------------------------------
    fun observeExpenses(userId: String): Flow<List<FirestoreExpense>> = callbackFlow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val reg = db.collection("users").document(userId).collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreTrackers", "Error listening to expenses", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            FirestoreExpense(
                                id = doc.id,
                                userId = userId,
                                title = doc.getString("title") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                category = doc.getString("category") ?: "General",
                                date = doc.getString("date") ?: "",
                                createdAt = doc.getString("createdAt") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveExpense(userId: String, title: String, amount: Double, category: String, date: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val id = "exp_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "amount" to amount,
            "category" to category,
            "date" to date,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        db.collection("users").document(userId).collection("expenses").document(id).set(data).await()
        return id
    }

    suspend fun deleteExpense(userId: String, id: String) {
        db.collection("users").document(userId).collection("expenses").document(id).delete().await()
    }

    // ---------------------------------
    // MEALS
    // ---------------------------------
    fun observeMeals(userId: String): Flow<List<FirestoreMeal>> = callbackFlow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val reg = db.collection("users").document(userId).collection("meals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreTrackers", "Error listening to meals", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            FirestoreMeal(
                                id = doc.id,
                                userId = userId,
                                mealType = doc.getString("mealType") ?: "Dinner",
                                title = doc.getString("title") ?: "",
                                recipe = doc.getString("recipe") ?: "",
                                date = doc.getString("date") ?: "",
                                createdAt = doc.getString("createdAt") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveMeal(userId: String, mealType: String, title: String, recipe: String, date: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val id = "meal_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "id" to id,
            "userId" to userId,
            "mealType" to mealType,
            "title" to title,
            "recipe" to recipe,
            "date" to date,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        db.collection("users").document(userId).collection("meals").document(id).set(data).await()
        return id
    }

    suspend fun deleteMeal(userId: String, id: String) {
        db.collection("users").document(userId).collection("meals").document(id).delete().await()
    }
}
