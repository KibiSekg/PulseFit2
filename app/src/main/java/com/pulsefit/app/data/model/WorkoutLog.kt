package com.pulsefit.app.data.model

/** Firestore: users/{userId}/workouts/{workoutId}. Also cached locally in Room (see WorkoutEntity). */
data class WorkoutLog(
    var workoutId: String = "",
    var userId: String = "",
    var exerciseId: String = "",
    var workoutType: String = "", // e.g. RUN, CYCLE, STRENGTH
    var durationMinutes: Long = 0,
    var distanceMeters: Double = 0.0,
    var caloriesBurned: Int = 0,
    var avgHeartRate: Int = 0,
    var workoutDate: Long = System.currentTimeMillis(),
    var synced: Boolean = true
)
