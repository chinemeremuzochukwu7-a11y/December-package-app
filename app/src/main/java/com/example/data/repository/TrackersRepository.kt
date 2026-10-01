package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BirthdayEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.MealEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TrackersRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val birthdayDao = db.birthdayDao()
    private val expenseDao = db.expenseDao()
    private val mealDao = db.mealDao()
    private val alarmDao = db.alarmDao()
    private val alarmScheduler = com.example.utils.AlarmScheduler(context)

    // --- Alarms & Reminders ---
    fun getAllAlarms(): Flow<List<com.example.data.local.AlarmEntity>> = alarmDao.getAllAlarms()

    fun getAlarmsByType(type: String): Flow<List<com.example.data.local.AlarmEntity>> = alarmDao.getAlarmsByType(type)

    suspend fun saveAlarm(alarm: com.example.data.local.AlarmEntity): Long {
        val id = alarmDao.insertAlarm(alarm)
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) {
            alarmScheduler.schedule(savedAlarm)
        }
        return id
    }

    suspend fun updateAlarm(alarm: com.example.data.local.AlarmEntity) {
        alarmDao.updateAlarm(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm.id)
        }
    }

    suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean) {
        alarmDao.updateEnabled(id, isEnabled)
        val alarm = alarmDao.getAlarmById(id)
        if (alarm != null) {
            if (isEnabled) {
                alarmScheduler.schedule(alarm.copy(isEnabled = true))
            } else {
                alarmScheduler.cancel(id)
            }
        }
    }

    suspend fun deleteAlarm(id: Long) {
        alarmScheduler.cancel(id)
        alarmDao.deleteById(id)
    }

    fun testAlarmSoundNow(title: String, type: String = "ALARM") {
        alarmScheduler.testAlarmNow(title, type)
    }

    // --- Birthdays ---
    fun getAllBirthdays(): Flow<List<BirthdayEntity>> = birthdayDao.getAllBirthdays()

    suspend fun saveBirthday(birthday: BirthdayEntity): Long = birthdayDao.insertBirthday(birthday)

    suspend fun updateBirthday(birthday: BirthdayEntity) = birthdayDao.updateBirthday(birthday)

    suspend fun deleteBirthday(id: Long) = birthdayDao.deleteById(id)

    // --- Expenses ---
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> = expenseDao.getExpensesByCategory(category)

    fun getTotalSpent(): Flow<Double?> = expenseDao.getTotalSpent()

    suspend fun saveExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(id: Long) = expenseDao.deleteById(id)

    // --- Daily Meals ---
    fun getMealsForDate(dateString: String): Flow<List<MealEntity>> = mealDao.getMealsForDate(dateString)

    fun getTotalCaloriesForDate(dateString: String): Flow<Int?> = mealDao.getTotalCaloriesForDate(dateString)

    suspend fun saveMeal(meal: MealEntity): Long = mealDao.insertMeal(meal)

    suspend fun updateMeal(meal: MealEntity) = mealDao.updateMeal(meal)

    suspend fun deleteMeal(id: Long) = mealDao.deleteById(id)

    companion object {
        /**
         * Calculates days until next birthday from today
         */
        fun daysUntilBirthday(month: Int, day: Int): Int {
            val today = Calendar.getInstance()
            val thisYear = today.get(Calendar.YEAR)
            val currentMonth = today.get(Calendar.MONTH) + 1 // 1-12
            val currentDay = today.get(Calendar.DAY_OF_MONTH)

            val bdayThisYear = Calendar.getInstance().apply {
                set(Calendar.YEAR, thisYear)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val todayZero = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (bdayThisYear.before(todayZero)) {
                // Birthday already passed this year, look at next year
                bdayThisYear.set(Calendar.YEAR, thisYear + 1)
            }

            val diffMillis = bdayThisYear.timeInMillis - todayZero.timeInMillis
            return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        }

        /**
         * Calculates age turning on upcoming birthday if year provided
         */
        fun calculateTurningAge(birthYear: Int?, month: Int, day: Int): Int? {
            if (birthYear == null || birthYear <= 1900) return null
            val today = Calendar.getInstance()
            val thisYear = today.get(Calendar.YEAR)
            val daysUntil = daysUntilBirthday(month, day)
            return if (daysUntil == 0) {
                thisYear - birthYear
            } else {
                val currentMonth = today.get(Calendar.MONTH) + 1
                val currentDay = today.get(Calendar.DAY_OF_MONTH)
                if (month < currentMonth || (month == currentMonth && day < currentDay)) {
                    (thisYear + 1) - birthYear
                } else {
                    thisYear - birthYear
                }
            }
        }

        fun getTodayDateString(): String {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
            return "$year-$month-$day"
        }
    }
}
