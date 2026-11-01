package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages Google Sign-In and silent account detection using Jetpack Credential Manager.
 * Guarantees zero anonymous auth fallbacks, safe error handling, and cloud data restoration.
 */
object AuthManager {

    private const val TAG = "AuthManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _lastAuthError = MutableStateFlow<String?>(null)
    val lastAuthError: StateFlow<String?> = _lastAuthError.asStateFlow()

    init {
        try {
            val auth = Firebase.auth
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
                Log.d(TAG, "Auth state changed. User: ${firebaseAuth.currentUser?.uid ?: "null"}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice setting up auth listener: ${e.message}")
        }
    }

    fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId)
            } else {
                context.getString(R.string.default_web_client_id)
            }
        } catch (e: Exception) {
            "308212671802-pnd9vt3s7030lgqetc2ghhuoc4f070ra.apps.googleusercontent.com"
        }
    }

    /**
     * Silent Auto-Sign-In on App Startup or after App Swiped / Cleared Storage.
     * Checks if the device has an authorized Google account and reconnects seamlessly.
     */
    fun attemptAutoSignIn(
        context: Context,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val auth = Firebase.auth
        val existing = auth.currentUser
        if (existing != null) {
            _currentUser.value = existing
            scope.launch(Dispatchers.IO) {
                try {
                    val repo = UserFirestoreRepository(context)
                    repo.ensureUserInitialized(existing)
                } catch (e: Exception) {
                    Log.w(TAG, "Notice syncing profile on auto-sign-in: ${e.message}")
                }
            }
            onComplete(true)
            return
        }

        val clientId = getWebClientId(context)
        if (clientId.isBlank()) {
            onComplete(false)
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    _currentUser.value = user
                    if (user != null) {
                        launch(Dispatchers.IO) {
                            try {
                                val repo = UserFirestoreRepository(context)
                                repo.ensureUserInitialized(user)
                            } catch (e: Exception) {
                                Log.w(TAG, "Notice initializing profile after auto sign-in: ${e.message}")
                            }
                        }
                    }
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                // Expected when no prior authorized account or credentials cleared
                Log.d(TAG, "Silent auto-sign-in skipped: ${e.message}")
                onComplete(false)
            }
        }
    }

    /**
     * Interactive Google Sign-In with visible Account Picker.
     * Uses Jetpack Credential Manager with GetSignInWithGoogleOption.
     */
    fun signInWithGoogle(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit = {},
        onError: (String) -> Unit = {},
        onCancelled: () -> Unit = {}
    ) {
        val clientId = getWebClientId(activity)
        if (clientId.isBlank()) {
            val msg = "Google Sign-In configuration missing (client ID)"
            _lastAuthError.value = msg
            onError(msg)
            return
        }

        _isAuthenticating.value = true
        _lastAuthError.value = null

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    _currentUser.value = user
                    _isAuthenticating.value = false

                    if (user != null) {
                        launch(Dispatchers.IO) {
                            try {
                                val repo = UserFirestoreRepository(activity)
                                repo.ensureUserInitialized(user)
                            } catch (e: Exception) {
                                Log.w(TAG, "Notice saving profile after interactive sign-in: ${e.message}")
                            }
                        }
                        onSuccess(user)
                    } else {
                        val err = "Authentication returned empty user"
                        _lastAuthError.value = err
                        onError(err)
                    }
                } else {
                    val err = "Unsupported credential returned"
                    _isAuthenticating.value = false
                    _lastAuthError.value = err
                    onError(err)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _isAuthenticating.value = false
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _isAuthenticating.value = false
                val errorMsg = e.localizedMessage ?: "Google Sign-In failed. Please try again."
                _lastAuthError.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    /**
     * Signs out from Firebase and clears Credential Manager session.
     */
    fun signOut(
        context: Context,
        onComplete: () -> Unit = {}
    ) {
        try {
            Firebase.auth.signOut()
            _currentUser.value = null
            val credentialManager = CredentialManager.create(context)
            scope.launch {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Notice clearing credential state: ${e.message}")
                } finally {
                    onComplete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign out", e)
            onComplete()
        }
    }
}
