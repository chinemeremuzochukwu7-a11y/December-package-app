package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.model.CardDecorationStyle
import com.example.model.CardOccasion
import com.example.model.CardTemplate
import com.example.model.CustomCardData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirestoreSavedCard(
    val id: String = "",
    val userId: String = "",
    val templateId: String = "",
    val title: String = "",
    val recipientName: String = "",
    val message: String = "",
    val senderName: String = "",
    val primaryColorHex: Long = 0xFFC41E3A,
    val secondaryColorHex: Long = 0xFF8B0000,
    val accentColorHex: Long = 0xFFFFD700,
    val decorationStyle: String = "STARS",
    val textSizeSp: Float = 15f,
    val textAlign: String = "Center",
    val showRecipient: Boolean = true,
    val showSender: Boolean = true,
    val createdAt: String = ""
) {
    fun toCustomCardData(): CustomCardData = CustomCardData(
        templateId = templateId,
        title = title,
        recipientName = recipientName,
        message = message,
        senderName = senderName,
        primaryColorHex = primaryColorHex,
        secondaryColorHex = secondaryColorHex,
        accentColorHex = accentColorHex,
        decorationStyle = try {
            CardDecorationStyle.valueOf(decorationStyle)
        } catch (_: Exception) {
            CardDecorationStyle.STARS
        },
        textSizeSp = textSizeSp,
        textAlign = textAlign,
        showRecipient = showRecipient,
        showSender = showSender
    )
}

class FirestoreCardsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreManager.getFirestore(context))

    /**
     * Observes real-time saved cards for the given user from Firestore.
     */
    fun observeSavedCards(userId: String): Flow<List<FirestoreSavedCard>> = callbackFlow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val collection = db.collection("users").document(userId).collection("saved_cards")
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreCardsRepo", "Error listening to saved cards", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cards = snapshot.documents.mapNotNull { doc ->
                    try {
                        FirestoreSavedCard(
                            id = doc.id,
                            userId = doc.getString("userId") ?: userId,
                            templateId = doc.getString("templateId") ?: "",
                            title = doc.getString("title") ?: "",
                            recipientName = doc.getString("recipientName") ?: "",
                            message = doc.getString("message") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            primaryColorHex = doc.getLong("primaryColorHex") ?: 0xFFC41E3A,
                            secondaryColorHex = doc.getLong("secondaryColorHex") ?: 0xFF8B0000,
                            accentColorHex = doc.getLong("accentColorHex") ?: 0xFFFFD700,
                            decorationStyle = doc.getString("decorationStyle") ?: "STARS",
                            textSizeSp = doc.getDouble("textSizeSp")?.toFloat() ?: 15f,
                            textAlign = doc.getString("textAlign") ?: "Center",
                            showRecipient = doc.getBoolean("showRecipient") ?: true,
                            showSender = doc.getBoolean("showSender") ?: true,
                            createdAt = doc.getString("createdAt") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(cards)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Saves or updates a card in Firestore under `/users/{userId}/saved_cards/{cardId}`.
     */
    suspend fun saveCard(userId: String, card: CustomCardData): String {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val cardId = "card_${System.currentTimeMillis()}"
        val nowString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        val data = hashMapOf(
            "id" to cardId,
            "userId" to userId,
            "templateId" to card.templateId,
            "title" to card.title,
            "recipientName" to card.recipientName,
            "message" to card.message,
            "senderName" to card.senderName,
            "primaryColorHex" to card.primaryColorHex,
            "secondaryColorHex" to card.secondaryColorHex,
            "accentColorHex" to card.accentColorHex,
            "decorationStyle" to card.decorationStyle.name,
            "textSizeSp" to card.textSizeSp,
            "textAlign" to card.textAlign,
            "showRecipient" to card.showRecipient,
            "showSender" to card.showSender,
            "createdAt" to nowString
        )

        db.collection("users").document(userId)
            .collection("saved_cards").document(cardId)
            .set(data)
            .await()

        return cardId
    }

    /**
     * Deletes a saved card from Firestore.
     */
    suspend fun deleteCard(userId: String, cardId: String) {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        require(cardId.isNotBlank()) { "Card ID must not be blank" }
        db.collection("users").document(userId)
            .collection("saved_cards").document(cardId)
            .delete()
            .await()
    }
}
