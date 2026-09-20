package com.pulsefit.app.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.data.model.MealLog
import com.pulsefit.app.data.repository.MealRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DietViewModel(db: AppDatabase) : ViewModel() {

    private val repo = MealRepository(db.mealDao())
    private val uid: String get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfToday(): Long = startOfToday() + 24 * 60 * 60 * 1000 - 1

    val todaysMeals: StateFlow<List<MealLog>> = repo
        .getMealsForDay(uid, startOfToday(), endOfToday())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caloriesConsumedToday: StateFlow<Double> = repo
        .getCaloriesConsumedToday(uid, startOfToday(), endOfToday())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun logMeal(name: String, type: String, calories: Double) {
        viewModelScope.launch {
            repo.logMeal(MealLog(userId = uid, mealName = name, mealType = type, calories = calories))
        }
    }
}
