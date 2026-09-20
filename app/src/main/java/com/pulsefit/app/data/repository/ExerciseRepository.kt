package com.pulsefit.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pulsefit.app.data.model.Exercise
import kotlinx.coroutines.tasks.await

/**
 * Reads the shared exercise catalog from Firestore's `exercises` collection.
 * TODO: add a Retrofit ExerciseDB service (data/remote/) and a one-off sync routine
 * that seeds/refreshes this collection from the ExerciseDB API, per the planning doc.
 */
class ExerciseRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun getExercises(muscleGroupFilter: String? = null): Result<List<Exercise>> = try {
        var query = firestore.collection("exercises").limit(50)
        if (!muscleGroupFilter.isNullOrBlank()) {
            query = query.whereEqualTo("muscleGroup", muscleGroupFilter)
        }
        val snapshot = query.get().await()
        Result.success(snapshot.toObjects(Exercise::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
