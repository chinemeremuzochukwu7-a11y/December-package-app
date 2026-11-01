package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.AuthManager
import com.google.firebase.auth.FirebaseUser

val RedDialogCrimson = Color(0xFF8B0000)
val RedDialogDeepWine = Color(0xFF4A0008)
val RedDialogGold = Color(0xFFFFD700)
val RedDialogWarmGold = Color(0xFFFFC107)
val RedDialogCardBg = Color(0xFF6B0F1A)

@Composable
fun GoogleSignInRedDialog(
    actionTitle: String = "Sign In to Create",
    actionDescription: String = "Please sign in with Google before creating custom holiday cards or generating AI wishes. This secures your 25 free credits and cloud backup!",
    onDismiss: () -> Unit,
    onSuccess: (FirebaseUser) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isAuthenticating by AuthManager.isAuthenticating.collectAsState()
    val lastError by AuthManager.lastAuthError.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isAuthenticating) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isAuthenticating,
            dismissOnClickOutside = !isAuthenticating,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, RedDialogGold.copy(alpha = 0.8f), RoundedCornerShape(28.dp))
                .shadow(24.dp, shape = RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(
                containerColor = RedDialogCardBg
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                RedDialogCrimson,
                                RedDialogDeepWine
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top close button & holiday badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RedDialogGold.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedDialogGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = RedDialogGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permanent Protection",
                                    color = RedDialogGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isAuthenticating,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close dialog",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Festive Icon with Gold Halo
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(RedDialogGold.copy(alpha = 0.15f))
                            .border(2.dp, RedDialogGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Holiday Gift & Auth",
                            tint = RedDialogGold,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = actionTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = actionDescription,
                        fontSize = 13.5.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Protection Benefit Chips in Red Theme
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitRow(
                            icon = Icons.Default.Shield,
                            title = "25 Free Starter Credits Safeguard",
                            subtitle = "Guaranteed credits remain yours even if you clear phone storage or reinstall."
                        )
                        BenefitRow(
                            icon = Icons.Default.CloudDone,
                            title = "Instant Cloud Sync for Cards & Wishes",
                            subtitle = "Access all your customized holiday cards across restarts and sessions."
                        )
                        BenefitRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "Seamless Account Auto-Detection",
                            subtitle = "App automatically reconnects to your account whenever you return."
                        )
                    }

                    // Display errors if any
                    val displayError = localError ?: lastError
                    AnimatedVisibility(visible = displayError != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            color = Color(0xFFD32F2F).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = displayError ?: "",
                                color = Color(0xFFFFCDD2),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Google Sign In Button in Premium Festive Red & White
                    Button(
                        onClick = {
                            if (activity != null) {
                                localError = null
                                AuthManager.signInWithGoogle(
                                    activity = activity,
                                    onSuccess = { user ->
                                        onSuccess(user)
                                        onDismiss()
                                    },
                                    onError = { err ->
                                        localError = err
                                    }
                                )
                            }
                        },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F1F1F)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = RedDialogCrimson,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Connecting to Google...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedDialogCrimson
                            )
                        } else {
                            // Google G Icon representation
                            Surface(
                                shape = CircleShape,
                                color = RedDialogCrimson.copy(alpha = 0.1f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = RedDialogCrimson
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isAuthenticating,
                        modifier = Modifier.testTag("dismiss_sign_in_dialog_button")
                    ) {
                        Text(
                            text = "Explore App First",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = RedDialogGold.copy(alpha = 0.2f),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RedDialogGold,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 15.sp
            )
        }
    }
}
