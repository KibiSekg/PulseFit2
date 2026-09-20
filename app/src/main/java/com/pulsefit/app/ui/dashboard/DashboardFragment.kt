package com.pulsefit.app.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.LocationServices
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.data.model.WeatherRisk
import com.pulsefit.app.data.model.assessRisk
import com.pulsefit.app.databinding.FragmentDashboardBinding
import com.pulsefit.app.ui.common.DbViewModelFactory
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DbViewModelFactory(AppDatabase.getInstance(requireContext())) { db -> DashboardViewModel(db) }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) fetchWeatherForCurrentLocation() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUserProfile()
            ensureLocationPermissionThenFetchWeather()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.startWorkoutButton.setOnClickListener {
            // TODO: navigate to an "in progress workout" screen once Route/Map tracking is wired up.
        }

        viewModel.loadUserProfile()
        ensureLocationPermissionThenFetchWeather()
        observeState()
    }

    private fun ensureLocationPermissionThenFetchWeather() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fetchWeatherForCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchWeatherForCurrentLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.loadWeather(location.latitude, location.longitude)
            } else {
                // Fallback: Johannesburg city center, used when no last-known location is cached yet.
                viewModel.loadWeather(-26.2041, 28.0473)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.dashboardProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    state.user?.let { user ->
                        binding.greetingText.text = "Hi, ${user.name.ifBlank { "there" }}"
                        binding.xpLevelText.text = "Lvl ${user.currentLevel} · ${user.totalXp} XP"
                    }

                    binding.caloriesBurnedText.text = "${state.caloriesBurnedToday} kcal"

                    state.weather?.let { weather ->
                        binding.weatherLocationText.text = weather.locationName
                        val condition = weather.weather.firstOrNull()?.description ?: "—"
                        binding.weatherDetailText.text =
                            "${weather.main.tempCelsius.toInt()}°C · $condition"

                        when (weather.assessRisk()) {
                            WeatherRisk.UNSAFE -> {
                                binding.weatherRiskBadge.visibility = View.VISIBLE
                                binding.weatherRiskBadge.text = "Unsafe — indoor alt. suggested"
                            }
                            WeatherRisk.CAUTION -> {
                                binding.weatherRiskBadge.visibility = View.VISIBLE
                                binding.weatherRiskBadge.text = "Caution"
                            }
                            WeatherRisk.SAFE -> binding.weatherRiskBadge.visibility = View.GONE
                        }
                    }

                    state.weatherError?.let {
                        binding.weatherLocationText.text = "Weather unavailable"
                        binding.weatherDetailText.text = it
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
