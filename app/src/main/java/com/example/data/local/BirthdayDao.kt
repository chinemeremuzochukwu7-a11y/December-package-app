package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Query("SELECT * FROM birthdays ORDER BY birthMonth ASC, birthDay ASC")
    fun getAllBirthdays(): Flow<List<BirthdayEntity>>

    @Query("SELECT * FROM birthdays WHERE id = :id LIMIT 1")
    suspend fun getBirthdayById(id: Long): BirthdayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: BirthdayEntity): Long

    @Update
    suspend fun updateBirthday(birthday: BirthdayEntity)

    @Delete
    suspend fun deleteBirthday(birthday: BirthdayEntity)

    @Query("DELETE FROM birthdays WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM birthdays")
    fun getBirthdayCount(): Flow<Int>
}
