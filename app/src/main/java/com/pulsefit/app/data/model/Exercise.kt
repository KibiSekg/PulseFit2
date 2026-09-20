package com.pulsefit.app.data.model

/** Firestore: exercises/{exerciseId} — seeded/synced from the ExerciseDB API. */
data class Exercise(
    var exerciseId: String = "",
    var name: String = "",
    var muscleGroup: String = "",
    var difficulty: String = "",
    var metRating: Double = 0.0, // metabolic equivalent, used for calorie calc
    var description: String = ""
)
