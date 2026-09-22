package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCardDao {
    @Query("SELECT * FROM saved_cards ORDER BY createdAt DESC")
    fun getAllSavedCards(): Flow<List<SavedCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SavedCardEntity)

    @Query("DELETE FROM saved_cards WHERE id = :id")
    suspend fun deleteCardById(id: String)

    @Query("SELECT COUNT(*) FROM saved_cards")
    suspend fun getCount(): Int
}
