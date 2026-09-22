package com.example.data.local

import com.example.model.Wish
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val dao: FavoriteWishDao) {

    val allFavorites: Flow<List<FavoriteWishEntity>> = dao.getAllFavorites()

    fun isFavorite(wishId: String): Flow<Boolean> = dao.isFavorite(wishId)

    suspend fun addFavorite(wish: Wish) {
        val entity = FavoriteWishEntity(
            id = wish.id,
            text = wish.text,
            category = wish.category.displayName,
            occasion = wish.occasion
        )
        dao.insertFavorite(entity)
    }

    suspend fun addCustomFavorite(id: String, text: String, category: String, occasion: String = "Generated") {
        val entity = FavoriteWishEntity(
            id = id,
            text = text,
            category = category,
            occasion = occasion
        )
        dao.insertFavorite(entity)
    }

    suspend fun removeFavorite(id: String) {
        dao.deleteFavoriteById(id)
    }

    suspend fun removeFavoriteByText(text: String) {
        dao.deleteFavoriteByText(text)
    }
}
