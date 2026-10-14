package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledGreetingDao {

    @Query("SELECT * FROM scheduled_greetings ORDER BY scheduledTimestamp ASC")
    fun getAllScheduledGreetings(): Flow<List<ScheduledGreetingEntity>>

    @Query("SELECT * FROM scheduled_greetings WHERE isDelivered = 0 AND scheduledTimestamp <= :currentTime")
    suspend fun getDueGreetings(currentTime: Long): List<ScheduledGreetingEntity>

    @Query("SELECT * FROM scheduled_greetings WHERE id = :id LIMIT 1")
    suspend fun getGreetingById(id: Long): ScheduledGreetingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGreeting(greeting: ScheduledGreetingEntity): Long

    @Update
    suspend fun updateGreeting(greeting: ScheduledGreetingEntity)

    @Query("UPDATE scheduled_greetings SET isDelivered = 1 WHERE id = :id")
    suspend fun markDelivered(id: Long)

    @Query("DELETE FROM scheduled_greetings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
