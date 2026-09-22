package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.HolidayCrimson
import com.example.ui.viewmodel.HolidayViewModel
import com.example.ui.viewmodel.SettingsDialogType

@Composable
fun SettingsScreen(
    viewModel: HolidayViewModel,
    modifier: Modifier = Modifier
) {
    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val countdownEnabled by viewModel.countdownEnabled.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
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
                    subtitle = "Version 1.0.0",
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
    when (activeDialog) {
        SettingsDialogType.ABOUT -> {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("About Holiday Wishes", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Holiday Wishes is a native Android application designed to help you create, discover, save, and share heartfelt Christmas and New Year wishes and greeting cards with friends, family, and colleagues.\n\nVersion: 1.0.0\nBuilt natively with Kotlin & Jetpack Compose\nLocal Offline Storage: Room Database\nNative Android Share System"
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
