package com.pulsefit.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationServices
import com.pulsefit.app.data.local.AppDatabase
import com.pulsefit.app.databinding.FragmentMapBinding
import com.pulsefit.app.ui.common.DbViewModelFactory

/**
 * Screen 3: Map & Live Tracking. Currently shows the user's live position and a
 * Start/Stop workout timer that logs a WorkoutLog on stop.
 *
 * TODO (per planning doc):
 *  - Google Maps Directions API: draw the route polyline between start/destination
 *  - Adaptive Weather Routing: cross-check DashboardViewModel's weather risk before
 *    confirming a route, and recalculate/suggest an indoor alternative if unsafe
 *  - Gamified Route Milestones: check proximity to waypoints and award Achievements
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null

    private var trackingStartElapsedMs: Long = 0L
    private var isTracking = false

    private val viewModel: MapViewModel by viewModels {
        DbViewModelFactory(AppDatabase.getInstance(requireContext())) { db -> MapViewModel(db) }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) centerOnCurrentLocation() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(com.pulsefit.app.R.id.map) as? SupportMapFragment)
            ?.getMapAsync(this)

        binding.startTrackingButton.setOnClickListener { startTracking() }
        binding.pauseTrackingButton.setOnClickListener { stopTracking() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        ensureLocationPermission()
    }

    private fun ensureLocationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) centerOnCurrentLocation()
        else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun centerOnCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        googleMap?.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener { location ->
                val target = if (location != null) LatLng(location.latitude, location.longitude)
                else LatLng(-26.2041, 28.0473) // fallback default
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 14f))
                googleMap?.addMarker(MarkerOptions().position(target).title("You are here"))
            }
    }

    private fun startTracking() {
        isTracking = true
        trackingStartElapsedMs = SystemClock.elapsedRealtime()
        binding.startTrackingButton.isEnabled = false
        binding.pauseTrackingButton.isEnabled = true
        // TODO: begin a periodic location callback here to build the route polyline and live distance.
    }

    private fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        val elapsedMinutes = (SystemClock.elapsedRealtime() - trackingStartElapsedMs) / 60000
        binding.startTrackingButton.isEnabled = true
        binding.pauseTrackingButton.isEnabled = false

        // Distance/calories are placeholders until the live polyline tracking above is wired up.
        viewModel.logCompletedWorkout(durationMinutes = elapsedMinutes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
