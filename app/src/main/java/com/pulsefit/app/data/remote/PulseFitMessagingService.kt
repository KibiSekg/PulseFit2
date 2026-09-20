package com.pulsefit.app.data.remote

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives Firebase Cloud Messaging pushes: weather alerts before planned workouts,
 * daily meal logging reminders, and community achievement updates (see planning doc,
 * "Real-time Notifications and Gamification").
 *
 * TODO: build a NotificationCompat.Builder + NotificationManager call here to actually
 * show the system notification, and write an AppNotification doc to Firestore for the
 * in-app notification history.
 */
class PulseFitMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // val title = message.notification?.title
        // val body = message.notification?.body
        // TODO: show a local notification with these values.
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: save this token to users/{uid}.fcmToken in Firestore so your backend
        // (or Cloud Functions) can target this device.
    }
}
