package com.pulsefit.app.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsefit.app.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Wraps Firebase Authentication (email/password + Google SSO) and creates the
 * matching Firestore user profile document on first registration, per the
 * "User Authentication and Security" requirement in the planning doc.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        heightCm: Double,
        weightKg: Double,
        primaryGoal: String
    ): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Registration failed — no user returned")

        val newUser = User(
            userId = firebaseUser.uid,
            name = name,
            email = email,
            heightCm = heightCm,
            weightKg = weightKg,
            primaryGoal = primaryGoal
        )
        firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
        Result.success(newUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Called after Google Sign-In returns an ID token; creates a profile doc if this is a first login. */
    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<User> = try {
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Google sign-in failed")

        val docRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = docRef.get().await()
        val user = if (snapshot.exists()) {
            snapshot.toObject(User::class.java)!!
        } else {
            val newUser = User(
                userId = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )
            docRef.set(newUser).await()
            newUser
        }
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchUserProfile(userId: String): Result<User> = try {
        val snapshot = firestore.collection("users").document(userId).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw IllegalStateException("Profile not found")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.userId).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() = auth.signOut()
}
