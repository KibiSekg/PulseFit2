package com.pulsefit.app.ui.diet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.databinding.DialogAddMealBinding
import com.pulsefit.app.databinding.FragmentDietBinding
import com.pulsefit.app.ui.common.DbViewModelFactory
import kotlinx.coroutines.launch

class DietFragment : Fragment() {

    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!
    private val adapter = MealsAdapter()

    private val viewModel: DietViewModel by viewModels {
        DbViewModelFactory(AppDatabase.getInstance(requireContext())) { db -> DietViewModel(db) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.mealsRecyclerView.adapter = adapter

        binding.addMealButton.setOnClickListener { showAddMealDialog() }

        observeState()
    }

    private fun showAddMealDialog() {
        val dialogBinding = DialogAddMealBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log a meal")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.mealNameInput.text.toString().trim()
                val type = dialogBinding.mealTypeInput.text.toString().trim().ifBlank { "SNACK" }
                val calories = dialogBinding.caloriesInput.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) viewModel.logMeal(name, type.uppercase(), calories)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.todaysMeals.collect { meals -> adapter.submitList(meals) }
                }
                launch {
                    viewModel.caloriesConsumedToday.collect { total ->
                        binding.caloriesConsumedText.text = "${total.toInt()} kcal consumed"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
