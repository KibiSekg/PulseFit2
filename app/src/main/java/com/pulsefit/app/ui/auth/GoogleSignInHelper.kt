package com.pulsefit.app.ui.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.pulsefit.app.BuildConfig

/**
 * Single Sign-On via Firebase Authentication, per requirement 1 of the planning doc.
 * GOOGLE_WEB_CLIENT_ID comes from local.properties (see README) — Firebase Console >
 * Authentication > Sign-in method > Google > Web SDK configuration.
 */
object GoogleSignInHelper {

    fun getClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun signInIntent(context: Context): Intent = getClient(context).signInIntent

    /** Call from the ActivityResultLauncher callback with the returned Intent data. */
    fun credentialFromIntent(data: Intent?): Result<AuthCredential> = try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        Result.success(GoogleAuthProvider.getCredential(account.idToken, null))
    } catch (e: ApiException) {
        Result.failure(e)
    }
}
