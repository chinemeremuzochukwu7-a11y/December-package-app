package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteWishDao {
    @Query("SELECT * FROM favorite_wishes ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteWishEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_wishes WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteWishEntity)

    @Query("DELETE FROM favorite_wishes WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("DELETE FROM favorite_wishes WHERE text = :text")
    suspend fun deleteFavoriteByText(text: String)
}
