package com.pulsefit.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pulsefit.app.data.local.AppDatabase

/** Small reusable factory for ViewModels that need the Room database injected. */
class DbViewModelFactory(
    private val db: AppDatabase,
    private val creator: (AppDatabase) -> ViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator(db) as T
}
