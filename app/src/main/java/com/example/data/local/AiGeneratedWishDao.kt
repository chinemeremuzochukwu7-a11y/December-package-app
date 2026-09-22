package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiGeneratedWishDao {
    @Query("SELECT * FROM ai_generated_wishes ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<AiGeneratedWishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWish(wish: AiGeneratedWishEntity)

    @Query("DELETE FROM ai_generated_wishes WHERE id = :id")
    suspend fun deleteWishById(id: String)

    @Query("DELETE FROM ai_generated_wishes")
    suspend fun clearAll()
}
