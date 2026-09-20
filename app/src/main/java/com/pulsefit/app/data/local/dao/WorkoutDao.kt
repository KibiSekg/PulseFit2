package com.pulsefit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulsefit.app.data.local.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity)

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY workoutDate DESC")
    fun getWorkoutsForUser(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<WorkoutEntity>

    @Query("UPDATE workout_logs SET pendingSync = 0 WHERE workoutId = :workoutId")
    suspend fun markSynced(workoutId: String)

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM workout_logs WHERE userId = :userId AND workoutDate BETWEEN :startOfDay AND :endOfDay")
    fun getCaloriesBurnedToday(userId: String, startOfDay: Long, endOfDay: Long): Flow<Int>
}
