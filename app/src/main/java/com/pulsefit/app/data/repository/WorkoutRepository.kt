package com.pulsefit.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pulsefit.app.data.local.WorkoutEntity
import com.pulsefit.app.data.local.dao.WorkoutDao
import com.pulsefit.app.data.local.toEntity
import com.pulsefit.app.data.local.toModel
import com.pulsefit.app.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Offline-first pattern from the planning doc: every write lands in Room immediately
 * (so the UI never blocks on network), then SyncWorker pushes pending rows to Firestore
 * once connectivity is confirmed.
 */
class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Save locally first (always succeeds offline); SyncWorker handles the Firestore push. */
    suspend fun logWorkout(workout: WorkoutLog): WorkoutLog {
        val withId = if (workout.workoutId.isBlank()) workout.copy(workoutId = UUID.randomUUID().toString()) else workout
        workoutDao.insert(withId.toEntity(pendingSync = true))
        return withId
    }

    fun getWorkoutsForUser(userId: String): Flow<List<WorkoutLog>> =
        workoutDao.getWorkoutsForUser(userId).map { list -> list.map { it.toModel() } }

    fun getCaloriesBurnedToday(userId: String, startOfDay: Long, endOfDay: Long): Flow<Int> =
        workoutDao.getCaloriesBurnedToday(userId, startOfDay, endOfDay)

    /** Called by SyncWorker. Pushes every locally-queued row to users/{uid}/workouts/{id}. */
    suspend fun syncPendingWorkouts() {
        val pending = workoutDao.getPendingSync()
        for (entity: WorkoutEntity in pending) {
            firestore.collection("users").document(entity.userId)
                .collection("workouts").document(entity.workoutId)
                .set(entity.toModel())
                .await()
            workoutDao.markSynced(entity.workoutId)
        }
    }
}
