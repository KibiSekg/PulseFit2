package com.pulsefit.app.data.model

/**
 * Firestore document: users/{userId}
 * Mirrors the "User" entity in the Data Schema (Planning & Design doc).
 */
data class User(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var language: String = "English",
    var unitSystem: String = "Metric", // "Metric" | "Imperial"
    var heightCm: Double = 0.0,
    var weightKg: Double = 0.0,
    var primaryGoal: String = "",
    var totalXp: Int = 0,
    var currentLevel: Int = 1,
    var createdAt: Long = System.currentTimeMillis()
)
// All fields have defaults, so Firestore's no-arg-constructor requirement
// for automatic deserialization (toObject<User>()) is satisfied automatically.
