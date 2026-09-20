package com.pulsefit.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.data.repository.MealRepository
import com.pulsefit.app.data.repository.WorkoutRepository

/**
 * Background sync per the planning doc: "A background WorkManager service monitors
 * connectivity changes and syncs queued SQLite records to Firestore once reconnected."
 * Scheduled as periodic work from PulseFitApp, and also triggered manually from
 * Settings > Force Manual Cloud Sync.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!NetworkUtils.isOnline(applicationContext)) {
            return Result.retry()
        }
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            WorkoutRepository(db.workoutDao()).syncPendingWorkouts()
            MealRepository(db.mealDao()).syncPendingMeals()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "pulsefit_sync_worker"
    }
}
