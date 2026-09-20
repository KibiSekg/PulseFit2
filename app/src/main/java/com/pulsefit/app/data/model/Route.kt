package com.pulsefit.app.data.model

/** Firestore: users/{userId}/routes/{routeId}. Populated from the Google Maps Directions API. */
data class Route(
    var routeId: String = "",
    var userId: String = "",
    var startLat: Double = 0.0,
    var startLng: Double = 0.0,
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var startLocationName: String = "",
    var destinationName: String = "",
    var totalDistanceM: Double = 0.0,
    var estimatedDurationMinutes: Int = 0,
    var polyline: String = "", // encoded polyline from Directions API
    var weatherAdjusted: Boolean = false, // true if Adaptive Weather Routing altered this route
    var createdAt: Long = System.currentTimeMillis()
)
