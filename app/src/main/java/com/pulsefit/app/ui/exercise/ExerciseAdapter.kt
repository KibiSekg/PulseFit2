package com.pulsefit.app.ui.exercise

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pulsefit.app.data.model.Exercise
import com.pulsefit.app.databinding.ItemExerciseBinding

class ExerciseAdapter : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(DIFF) {

    class ExerciseViewHolder(val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)
        holder.binding.exerciseNameText.text = exercise.name
        holder.binding.exerciseMuscleGroupText.text = "Target: ${exercise.muscleGroup}"
        holder.binding.exerciseMetText.text = "MET: ${exercise.metRating}"
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Exercise>() {
            override fun areItemsTheSame(old: Exercise, new: Exercise) = old.exerciseId == new.exerciseId
            override fun areContentsTheSame(old: Exercise, new: Exercise) = old == new
        }
    }
}
