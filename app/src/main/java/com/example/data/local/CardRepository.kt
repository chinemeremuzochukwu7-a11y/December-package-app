package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CardRepository(private val dao: SavedCardDao) {
    val allSavedCards: Flow<List<SavedCardEntity>> = dao.getAllSavedCards()

    suspend fun saveCard(card: SavedCardEntity) {
        dao.insertCard(card)
    }

    suspend fun deleteCard(id: String) {
        dao.deleteCardById(id)
    }
}
