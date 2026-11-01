package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.subscription.SubscriptionManager
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Star
import com.example.ui.components.ReportProblemDialog
import com.example.utils.RateAppManager
import com.example.ui.theme.HolidayCrimson
import com.example.ui.viewmodel.HolidayViewModel
import com.example.ui.viewmodel.SettingsDialogType

import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import android.widget.Toast
import com.example.data.firebase.AuthManager
import com.example.ui.components.GoogleSignInRedDialog
import com.example.ui.theme.HolidayGold

@Composable
fun SettingsScreen(
    viewModel: HolidayViewModel,
    onNavigateToPro: () -> Unit = {},
    onNavigateToCustomerCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPro by SubscriptionManager.isPro.collectAsState()
    val credits by SubscriptionManager.credits.collectAsState()
    val isCloudSynced by SubscriptionManager.isCloudSynced.collectAsState()
    val isSyncing by SubscriptionManager.isSyncing.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsStateWithLifecycle()
    var showGoogleSignInDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val deviceId = remember { com.example.data.firebase.UserFirestoreRepository.getOrCreateUserId(context) }
    var restoreDialogMessage by remember { mutableStateOf<String?>(null) }
    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val countdownEnabled by viewModel.countdownEnabled.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var showReportProblemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Dedicated Festive Red Google Account Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("settings_google_account_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, HolidayGold)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8B0000), // Holiday Crimson
                                Color(0xFF5A000A)  // Deep Wine
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                if (currentUser != null) {
                    val user = currentUser!!
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(HolidayGold.copy(alpha = 0.2f))
                                    .border(2.dp, HolidayGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = HolidayGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName?.ifBlank { "Holiday Member" } ?: "Holiday Member",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Account",
                                        tint = HolidayGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = user.email ?: "Google Account Connected",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Permanent Credit & Card Cloud Protection Active ✓",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HolidayGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🪙", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$credits Credits Synced",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    AuthManager.signOut(context) {
                                        Toast.makeText(context, "Signed out from Google", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.testTag("settings_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sign Out",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(HolidayGold.copy(alpha = 0.2f))
                                    .border(2.dp, HolidayGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = HolidayGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Connect Google Account",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Sign in to protect your 25 starter credits and back up all custom cards & wishes to the cloud.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showGoogleSignInDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF8B0000)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("settings_google_sign_in_button")
                        ) {
                            Text(
                                text = "Sign In with Google",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Holiday Wishes Pro Membership Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPro() }
                .testTag("settings_pro_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isPro) {
                                listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                            } else {
                                listOf(Color(0xFF8B0000), Color(0xFFC41E3A), Color(0xFFD4AF37))
                            }
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isPro) "👑" else "✨", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPro) "Holiday Wishes Pro Active" else "Holiday Wishes Pro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isPro)
                                "Entitlement: holiday_wishes_pro active"
                            else
                                "Unlock VIP cards, unlimited AI, and ad-free experience",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToPro,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF8B0000)
                        )
                    ) {
                        Text(
                            text = if (isPro) "View" else "Upgrade",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Preferences Card Group
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = selectedLanguage,
                    onClick = { viewModel.openDialog(SettingsDialogType.LANGUAGE) },
                    testTag = "settings_language"
                )

                SettingsDivider()

                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Holiday Wishes",
                    subtitle = "Get a daily festive quote reminder",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() },
                    testTag = "settings_notifications_toggle"
                )

                SettingsDivider()

                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "New Year Countdown",
                    subtitle = "Notifications as New Year approaches",
                    isChecked = countdownEnabled,
                    onCheckedChange = { viewModel.toggleCountdown() },
                    testTag = "settings_countdown_toggle"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Subscription & Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Pro Membership & Plans",
                    subtitle = if (isPro) "Active (Holiday Wishes Pro)" else "Unlock Monthly ($9.99) or Yearly ($79 - Save 34%)",
                    onClick = onNavigateToPro,
                    testTag = "settings_plans_item"
                )

                SettingsDivider()

                SettingsClickableItem(
                    icon = Icons.Default.SupportAgent,
                    title = "Customer Center",
                    subtitle = "Manage billing, request refunds, or get help",
                    onClick = onNavigateToCustomerCenter,
                    testTag = "settings_customer_center_item"
                )

                SettingsDivider()

                SettingsClickableItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Problem",
                    subtitle = "Found a bug or issue? Tell our engineers",
                    onClick = { showReportProblemDialog = true },
                    testTag = "settings_report_problem_item"
                )

                SettingsDivider()

                SettingsClickableItem(
                    icon = Icons.Default.Star,
                    title = "Rate for Us",
                    subtitle = "Rate Holiday Wishes on Google Play ⭐",
                    onClick = { RateAppManager.showRatingPromptExplicitly("Rate Holiday Wishes 🌟") },
                    testTag = "settings_rate_for_us_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Data & Credit Cloud Protection",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_credit_protection_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCloudSynced) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                else HolidayCrimson.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isCloudSynced) "🛡️" else "🔄", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hardware Cloud Protection",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCloudSynced) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isCloudSynced) "PROTECTED ✓" else "SYNCING...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isCloudSynced) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Credits: $credits AI Credits available",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your credit balance and Pro entitlements are tied to your device hardware identity. Even if you clear phone storage or reinstall the app, your credits are automatically preserved and restored from Cloud Firestore without needing passwords.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device: ${deviceId.take(14)}...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )

                    OutlinedButton(
                        onClick = {
                            SubscriptionManager.manualRestoreCredits(context) { success, balance, msg ->
                                restoreDialogMessage = msg
                            }
                        },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("settings_sync_credits_button")
                    ) {
                        if (isSyncing) {
                            Text(text = "Syncing...", fontSize = 12.sp)
                        } else {
                            Text(text = "Verify & Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "About & Legal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "About Holiday Wishes",
                    subtitle = "Version ${com.example.BuildConfig.VERSION_NAME}",
                    onClick = { viewModel.openDialog(SettingsDialogType.ABOUT) },
                    testTag = "settings_about"
                )

                SettingsDivider()

                SettingsClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "100% Offline, no personal data collected",
                    onClick = { viewModel.openDialog(SettingsDialogType.PRIVACY_POLICY) },
                    testTag = "settings_privacy"
                )

                SettingsDivider()

                SettingsClickableItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Usage rules and licensing",
                    onClick = { viewModel.openDialog(SettingsDialogType.TERMS_OF_SERVICE) },
                    testTag = "settings_terms"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // App Footer branding
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎄 Holiday Wishes Native Android App",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Spread love, cheer, and joy across the world.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    // Dialogs
    if (restoreDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { restoreDialogMessage = null },
            title = { Text("Credit Protection Status", fontWeight = FontWeight.Bold) },
            text = { Text(restoreDialogMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { restoreDialogMessage = null }) {
                    Text("OK", color = HolidayCrimson, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showReportProblemDialog) {
        ReportProblemDialog(onDismiss = { showReportProblemDialog = false })
    }

    when (activeDialog) {
        SettingsDialogType.ABOUT -> {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("About Holiday Wishes", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Holiday Wishes is a native Android application designed to help you create, discover, save, and share heartfelt Christmas and New Year wishes and greeting cards with friends, family, and colleagues.\n\nVersion: ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})\nBuilt natively with Kotlin & Jetpack Compose\nLocal Offline Storage: Room Database\nNative Android Share System"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("OK", color = HolidayCrimson, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        SettingsDialogType.LANGUAGE -> {
            val languages = listOf("English", "Español", "Français", "Deutsch")
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("Select Language", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        languages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = lang == selectedLanguage,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(selectedColor = HolidayCrimson)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = lang, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Close", color = HolidayCrimson)
                    }
                }
            )
        }
        SettingsDialogType.PRIVACY_POLICY -> {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Holiday Wishes is completely privacy-first:\n\n• 100% Offline Functionality: Your saved favorites and customized greeting cards are stored strictly on your local device via Room Database.\n• No external trackers, accounts, or analytics are used.\n• Sharing is processed directly through Android's native share sheet.\n• We do not collect, sell, or transmit any personal data."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("I Understand", color = HolidayCrimson, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        SettingsDialogType.TERMS_OF_SERVICE -> {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "By using Holiday Wishes, you are free to share, personalize, and distribute wishes and cards created in the application for personal and non-commercial communications.\n\nAll templates and wishes are provided for spreading holiday goodwill and festive cheer."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Accept", color = HolidayCrimson, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        null -> {}
        else -> {}
    }

    if (showGoogleSignInDialog) {
        GoogleSignInRedDialog(
            actionTitle = "Google Account Sync",
            actionDescription = "Connect your Google account to secure your 25 starter credits and back up all custom cards & wishes across devices and phone resets.",
            onDismiss = { showGoogleSignInDialog = false }
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HolidayCrimson,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HolidayCrimson,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = HolidayCrimson)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    ) {}
}
