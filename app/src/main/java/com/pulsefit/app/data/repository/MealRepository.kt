package com.pulsefit.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pulsefit.app.data.local.MealEntity
import com.pulsefit.app.data.local.dao.MealDao
import com.pulsefit.app.data.local.toEntity
import com.pulsefit.app.data.local.toModel
import com.pulsefit.app.data.model.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MealRepository(
    private val mealDao: MealDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun logMeal(meal: MealLog): MealLog {
        val withId = if (meal.mealId.isBlank()) meal.copy(mealId = UUID.randomUUID().toString()) else meal
        mealDao.insert(withId.toEntity(pendingSync = true))
        return withId
    }

    fun getMealsForDay(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<MealLog>> =
        mealDao.getMealsForDay(userId, startOfDay, endOfDay).map { list -> list.map { it.toModel() } }

    fun getCaloriesConsumedToday(userId: String, startOfDay: Long, endOfDay: Long): Flow<Double> =
        mealDao.getCaloriesConsumedToday(userId, startOfDay, endOfDay)

    suspend fun syncPendingMeals() {
        val pending = mealDao.getPendingSync()
        for (entity: MealEntity in pending) {
            firestore.collection("users").document(entity.userId)
                .collection("meals").document(entity.mealId)
                .set(entity.toModel())
                .await()
            mealDao.markSynced(entity.mealId)
        }
    }
}
