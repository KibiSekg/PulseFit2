package com.pulsefit.app.ui.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pulsefit.app.data.model.MealLog
import com.pulsefit.app.databinding.ItemMealBinding

class MealsAdapter : ListAdapter<MealLog, MealsAdapter.MealViewHolder>(DIFF) {

    class MealViewHolder(val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = getItem(position)
        holder.binding.mealNameText.text = meal.mealName
        holder.binding.mealTypeText.text = meal.mealType
        holder.binding.mealCaloriesText.text = "${meal.getTotalCalories()} kcal"
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MealLog>() {
            override fun areItemsTheSame(old: MealLog, new: MealLog) = old.mealId == new.mealId
            override fun areContentsTheSame(old: MealLog, new: MealLog) = old == new
        }
    }
}
