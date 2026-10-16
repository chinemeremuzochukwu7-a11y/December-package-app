package com.example.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.firebase.UserFirestoreRepository
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayCrimsonDark
import com.example.ui.theme.HolidayGold
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userRepo = remember { UserFirestoreRepository(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showEmailAuth by remember { mutableStateOf(false) }

    val performGoogleSignIn: () -> Unit = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = context.getString(R.string.default_web_client_id)

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()

                    val user = authResult.user
                    if (user != null) {
                        userRepo.ensureUserInitialized(user)
                        Toast.makeText(context, "Welcome, ${user.displayName ?: "Holiday Friend"}! 25 Credits Added 🪙", Toast.LENGTH_LONG).show()
                        onSignInSuccess()
                    }
                } else {
                    errorMessage = "Unexpected credential format returned"
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("AuthScreen", "Sign in canceled by user")
            } catch (e: GetCredentialException) {
                Log.w("AuthScreen", "CredentialManager error: ${e.message}", e)
                errorMessage = "Google Sign-In: ${e.localizedMessage ?: "Sign-in error"}"
            } catch (e: Exception) {
                Log.e("AuthScreen", "Firebase Auth failed", e)
                errorMessage = e.localizedMessage ?: "Authentication failed"
            } finally {
                isLoading = false
            }
        }
    }

    val performEmailSignIn: () -> Unit = {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
        } else {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val authResult = FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email.trim(), password.trim())
                        .await()
                    val user = authResult.user
                    if (user != null) {
                        userRepo.ensureUserInitialized(user)
                        Toast.makeText(context, "Welcome back! 25 Credits Active 🪙", Toast.LENGTH_LONG).show()
                        onSignInSuccess()
                    }
                } catch (e: Exception) {
                    Log.e("AuthScreen", "Email sign-in failed", e)
                    errorMessage = e.localizedMessage ?: "Invalid email or password"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val performEmailSignUp: () -> Unit = {
        if (email.isBlank() || password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
        } else {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val authResult = FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.trim(), password.trim())
                        .await()
                    val user = authResult.user
                    if (user != null) {
                        userRepo.ensureUserInitialized(user)
                        Toast.makeText(context, "Welcome! 25 Free Credits added to your Firestore account 🪙", Toast.LENGTH_LONG).show()
                        onSignInSuccess()
                    }
                } catch (e: Exception) {
                    Log.e("AuthScreen", "Email sign-up failed", e)
                    errorMessage = e.localizedMessage ?: "Account creation failed"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val performQuickTestSignIn: () -> Unit = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val auth = FirebaseAuth.getInstance()
                val testEmail = "holiday.user@gmail.com"
                val testPass = "Holiday2026!"
                val authResult = try {
                    auth.signInWithEmailAndPassword(testEmail, testPass).await()
                } catch (_: Exception) {
                    try {
                        auth.createUserWithEmailAndPassword(testEmail, testPass).await()
                    } catch (_: Exception) {
                        auth.signInWithEmailAndPassword(testEmail, testPass).await()
                    }
                }
                val user = authResult.user
                if (user != null) {
                    userRepo.ensureUserInitialized(user)
                    Toast.makeText(context, "Welcome! 25 Free Credits Added to Firestore 🪙", Toast.LENGTH_LONG).show()
                    onSignInSuccess()
                }
            } catch (e: Exception) {
                Log.w("AuthScreen", "Quick sign-in fallback: ${e.message}")
                errorMessage = e.localizedMessage ?: "Instant sign-in failed"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HolidayCrimsonDark,
                        HolidayCrimson,
                        Color(0xFF2A0008)
                    )
                )
            )
            .safeDrawingPadding()
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon & Festive Badge
            Surface(
                shape = CircleShape,
                color = HolidayGold.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, HolidayGold),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🎄", fontSize = 44.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Holiday Wishes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )

            Text(
                text = "1,000+ Luxury Cards, AI Wishes & Cloud Sync",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 25 Credits Welcome Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New User Welcome Gift",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HolidayGold
                        )
                    }

                    Text(
                        text = "Sign in now to instantly receive 25 Free AI Credits on Firestore! Customize luxury cards, generate greetings, and save everything safely in the cloud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HolidayGold,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "🪙 25 Free Credits Upon Sign In",
                            color = Color(0xFF3E2723),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (isLoading) {
                CircularProgressIndicator(color = HolidayGold, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Connecting to Firebase Firestore...",
                    color = Color.White,
                    fontSize = 13.sp
                )
            } else {
                // Google Sign In Button
                Button(
                    onClick = performGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instant Demo Sign In Button (Fast testing in preview emulator)
                Button(
                    onClick = performQuickTestSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HolidayGold,
                        contentColor = Color(0xFF3E2723)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quick_sign_in_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "1-Tap Instant Sign In (25 Credits 🪙)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF3E2723)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Email / Password section
                TextButton(
                    onClick = { showEmailAuth = !showEmailAuth },
                    modifier = Modifier.testTag("toggle_email_auth")
                ) {
                    Text(
                        text = if (showEmailAuth) "Hide Email Sign In ▲" else "Or Sign In with Email / Password ▼",
                        color = HolidayGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                if (showEmailAuth) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = HolidayGold)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = HolidayGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input")
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = HolidayGold)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = HolidayGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = performEmailSignIn,
                                    colors = ButtonDefaults.buttonColors(containerColor = HolidayGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("email_sign_in_button")
                                ) {
                                    Text("Sign In", color = Color(0xFF3E2723), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = performEmailSignUp,
                                    border = BorderStroke(1.dp, HolidayGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("email_sign_up_button")
                                ) {
                                    Text("Sign Up (+25🪙)", color = HolidayGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFFB4AB),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                    Text(
                        text = " SECURE CLOUD STORAGE ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = HolidayGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Realtime Firestore Cloud Database • 25 Credits Initial Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
