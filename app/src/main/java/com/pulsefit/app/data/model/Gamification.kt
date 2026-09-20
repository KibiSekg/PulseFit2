package com.pulsefit.app.data.model

/** Firestore: challenges/{challengeId} — global, not per-user. */
data class Challenge(
    var challengeId: String = "",
    var name: String = "",
    var description: String = "",
    var target: Double = 0.0,
    var rewardXp: Int = 0
)

/** Firestore: users/{userId}/userChallenges/{userChallengeId} */
data class UserChallenge(
    var userChallengeId: String = "",
    var userId: String = "",
    var challengeId: String = "",
    var progress: Double = 0.0,
    var status: String = "ACTIVE", // ACTIVE | COMPLETED
    var dateJoined: Long = System.currentTimeMillis()
)

/** Firestore: users/{userId}/achievements/{achievementId} — earned via Gamified Route Milestones. */
data class Achievement(
    var achievementId: String = "",
    var userId: String = "",
    var badgeName: String = "",
    var xpEarned: Int = 0,
    var dateEarned: Long = System.currentTimeMillis()
)

/** Firestore: users/{userId}/notifications/{notificationId} — mirrors FCM pushes for in-app history. */
data class AppNotification(
    var notificationId: String = "",
    var userId: String = "",
    var title: String = "",
    var message: String = "",
    var notificationType: String = "", // WEATHER_ALERT | MEAL_REMINDER | ACHIEVEMENT
    var dateSent: Long = System.currentTimeMillis(),
    var read: Boolean = false
)
