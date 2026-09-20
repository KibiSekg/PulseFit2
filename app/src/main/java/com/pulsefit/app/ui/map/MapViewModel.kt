package com.pulsefit.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.data.model.WorkoutLog
import com.pulsefit.app.data.repository.WorkoutRepository
import kotlinx.coroutines.launch

class MapViewModel(db: AppDatabase) : ViewModel() {

    private val workoutRepo = WorkoutRepository(db.workoutDao())

    /**
     * Saves the just-finished tracking session as a WorkoutLog (offline-first — see
     * WorkoutRepository). Distance/calories are 0 until live polyline tracking (TODO
     * in MapFragment) supplies real GPS-derived values.
     */
    fun logCompletedWorkout(durationMinutes: Long, distanceMeters: Double = 0.0, caloriesBurned: Int = 0) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (durationMinutes <= 0) return

        viewModelScope.launch {
            workoutRepo.logWorkout(
                WorkoutLog(
                    userId = uid,
                    workoutType = "RUN",
                    durationMinutes = durationMinutes,
                    distanceMeters = distanceMeters,
                    caloriesBurned = caloriesBurned
                )
            )
        }
    }
}
