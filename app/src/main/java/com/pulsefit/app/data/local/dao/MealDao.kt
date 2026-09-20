package com.pulsefit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulsefit.app.data.local.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealEntity)

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND mealDate BETWEEN :startOfDay AND :endOfDay ORDER BY mealDate ASC")
    fun getMealsForDay(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meal_logs WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<MealEntity>

    @Query("UPDATE meal_logs SET pendingSync = 0 WHERE mealId = :mealId")
    suspend fun markSynced(mealId: String)

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_logs WHERE userId = :userId AND mealDate BETWEEN :startOfDay AND :endOfDay")
    fun getCaloriesConsumedToday(userId: String, startOfDay: Long, endOfDay: Long): Flow<Double>
}
