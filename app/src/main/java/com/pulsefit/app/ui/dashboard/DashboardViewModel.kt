package com.pulsefit.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.data.model.User
import com.pulsefit.app.data.model.WeatherResponse
import com.pulsefit.app.data.repository.AuthRepository
import com.pulsefit.app.data.repository.WeatherRepository
import com.pulsefit.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val user: User? = null,
    val weather: WeatherResponse? = null,
    val weatherError: String? = null,
    val caloriesBurnedToday: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val db: AppDatabase,
    private val authRepo: AuthRepository = AuthRepository(),
    private val weatherRepo: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val workoutRepo = WorkoutRepository(db.workoutDao())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfToday(): Long = startOfToday() + 24 * 60 * 60 * 1000 - 1

    fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            authRepo.fetchUserProfile(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
            }
        }
        viewModelScope.launch {
            workoutRepo.getCaloriesBurnedToday(uid, startOfToday(), endOfToday()).collect { calories ->
                _uiState.value = _uiState.value.copy(caloriesBurnedToday = calories)
            }
        }
    }

    /** Powers Adaptive Weather Routing: fetches conditions at the user's current location. */
    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepo.getCurrentWeather(lat, lon)
                .onSuccess { _uiState.value = _uiState.value.copy(weather = it, weatherError = null) }
                .onFailure { _uiState.value = _uiState.value.copy(weatherError = it.message ?: "Weather unavailable") }
        }
    }
}
