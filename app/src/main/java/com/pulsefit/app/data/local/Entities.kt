package com.pulsefit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pulsefit.app.data.model.MealLog
import com.pulsefit.app.data.model.WorkoutLog

/**
 * Local Room cache for workouts logged while offline. `pendingSync = true` means
 * this row hasn't been pushed to Firestore yet; SyncWorker clears the flag once it has.
 */
@Entity(tableName = "workout_logs")
data class WorkoutEntity(
    @PrimaryKey val workoutId: String,
    val userId: String,
    val exerciseId: String,
    val workoutType: String,
    val durationMinutes: Long,
    val distanceMeters: Double,
    val caloriesBurned: Int,
    val avgHeartRate: Int,
    val workoutDate: Long,
    val pendingSync: Boolean = true
)

@Entity(tableName = "meal_logs")
data class MealEntity(
    @PrimaryKey val mealId: String,
    val userId: String,
    val mealType: String,
    val mealName: String,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val mealDate: Long,
    val pendingSync: Boolean = true
)

fun WorkoutEntity.toModel() = WorkoutLog(
    workoutId, userId, exerciseId, workoutType, durationMinutes,
    distanceMeters, caloriesBurned, avgHeartRate, workoutDate, synced = !pendingSync
)

fun WorkoutLog.toEntity(pendingSync: Boolean = false) = WorkoutEntity(
    workoutId, userId, exerciseId, workoutType, durationMinutes,
    distanceMeters, caloriesBurned, avgHeartRate, workoutDate, pendingSync
)

fun MealEntity.toModel() = MealLog(
    mealId, userId, mealType, mealName, calories,
    proteinGrams, carbsGrams, fatGrams, mealDate, synced = !pendingSync
)

fun MealLog.toEntity(pendingSync: Boolean = false) = MealEntity(
    mealId, userId, mealType, mealName, calories,
    proteinGrams, carbsGrams, fatGrams, mealDate, pendingSync
)
