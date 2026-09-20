package com.pulsefit.app.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsefit.app.data.model.Exercise
import com.pulsefit.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repo: ExerciseRepository = ExerciseRepository()) : ViewModel() {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    init {
        loadExercises(null)
    }

    fun loadExercises(muscleGroupFilter: String?) {
        viewModelScope.launch {
            repo.getExercises(muscleGroupFilter).onSuccess { _exercises.value = it }
        }
    }
}
