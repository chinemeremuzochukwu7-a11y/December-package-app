package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.data.repository.TrackersRepository
import com.example.receiver.AlarmReceiver
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmReminderScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { TrackersRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Alarms, 1 = Reminders
    val allAlarms by repository.getAllAlarms().collectAsState(initial = emptyList())

    val filteredList = remember(allAlarms, selectedTab) {
        val targetType = if (selectedTab == 0) "ALARM" else "REMINDER"
        allAlarms.filter { it.type == targetType }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var isTestingSound by remember { mutableStateOf(false) }

    val holidayPresets = remember {
        listOf(
            Triple("Christmas Morning", 7 to 0, "ALARM"),
            Triple("New Year Countdown", 23 to 45, "ALARM"),
            Triple("Gift Wrapping Time", 20 to 0, "REMINDER"),
            Triple("Feast Cooking Alarm", 10 to 30, "ALARM"),
            Triple("Call Family & Friends", 9 to 15, "REMINDER")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = if (selectedTab == 0) HolidayCrimson else HolidayPineGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_alarm_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm or Reminder")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (selectedTab == 0)
                                    listOf(Color(0xFF8B0000), Color(0xFFC41E3A), Color(0xFFD4AF37))
                                else
                                    listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFFD4AF37))
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (selectedTab == 0) "⏰" else "🔔", fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedTab == 0) "Holiday Alarms" else "Event Reminders",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (selectedTab == 0)
                                    "Loud sound & vibration when the scheduled time arrives"
                                else
                                    "Gentle notification alerts for holiday tasks & calls",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }

                        // Test Sound Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clickable {
                                    if (!isTestingSound) {
                                        isTestingSound = true
                                        repository.testAlarmSoundNow(
                                            title = if (selectedTab == 0) "Test Holiday Alarm" else "Test Holiday Reminder",
                                            type = if (selectedTab == 0) "ALARM" else "REMINDER"
                                        )
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Alarm test ringing! Tap 'Stop' to dismiss.")
                                        }
                                    } else {
                                        isTestingSound = false
                                        AlarmReceiver.stopAlarm()
                                    }
                                }
                                .testTag("test_alarm_sound_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTestingSound) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) HolidayCrimson else HolidayPineGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTestingSound) "Stop" else "Test",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) HolidayCrimson else HolidayPineGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Tab Selector: Alarms vs Reminders
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Alarms (Loud Sound)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reminders (Alerts)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Presets Row
            Text(
                text = "Quick Holiday Presets",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(holidayPresets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.saveAlarm(
                                    AlarmEntity(
                                        title = preset.first,
                                        type = preset.third,
                                        hour = preset.second.first,
                                        minute = preset.second.second,
                                        isEnabled = true,
                                        repeatMode = "Daily"
                                    )
                                )
                                snackbarHostState.showSnackbar("Scheduled: ${preset.first} at ${formatTime(preset.second.first, preset.second.second)}")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (preset.third == "ALARM") "⏰" else "🔔", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${preset.first} (${formatTime(preset.second.first, preset.second.second)})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Alarms / Reminders List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = if (selectedTab == 0) "⏰" else "🔔", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "No alarms set yet" else "No reminders set yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedTab == 0)
                                "Set holiday wake-up alarms. When the time arrives, the alarm will ring and sound alerts immediately."
                            else
                                "Set reminders for holiday gift shopping, sending wishes, and calling friends.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == 0) HolidayCrimson else HolidayPineGreen
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedTab == 0) "Set Alarm" else "Set Reminder")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = { isChecked ->
                                scope.launch {
                                    repository.toggleAlarmEnabled(alarm.id, isChecked)
                                    val statusMsg = if (isChecked) "Enabled for ${formatTime(alarm.hour, alarm.minute)}" else "Turned off"
                                    snackbarHostState.showSnackbar("${alarm.title}: $statusMsg")
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteAlarm(alarm.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            defaultType = if (selectedTab == 0) "ALARM" else "REMINDER",
            onDismiss = { showAddDialog = false },
            onSave = { title, hour, minute, type, repeatMode, notes ->
                scope.launch {
                    repository.saveAlarm(
                        AlarmEntity(
                            title = title,
                            type = type,
                            hour = hour,
                            minute = minute,
                            repeatMode = repeatMode,
                            isEnabled = true,
                            notes = notes
                        )
                    )
                    showAddDialog = false
                    snackbarHostState.showSnackbar("Alarm scheduled for ${formatTime(hour, minute)}")
                }
            }
        )
    }
}

@Composable
private fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (alarm.isEnabled)
                if (alarm.type == "ALARM") HolidayCrimson.copy(alpha = 0.4f) else HolidayPineGreen.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_item_${alarm.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTime(alarm.hour, alarm.minute),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (alarm.isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (alarm.type == "ALARM") HolidayCrimson.copy(alpha = 0.15f) else HolidayPineGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = alarm.repeatMode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (alarm.type == "ALARM") HolidayCrimson else HolidayPineGreen
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (alarm.notes.isNotEmpty()) {
                    Text(
                        text = alarm.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Enable/Disable Switch
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (alarm.type == "ALARM") HolidayCrimson else HolidayPineGreen
                ),
                modifier = Modifier.testTag("alarm_switch_${alarm.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlarmDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (title: String, hour: Int, minute: Int, type: String, repeatMode: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var isPm by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(defaultType) }
    var repeatMode by remember { mutableStateOf("Daily") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val repeatModes = listOf("Daily", "Once", "Weekdays", "Weekends")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (type == "ALARM") "⏰" else "🔔", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (type == "ALARM") "Set Holiday Alarm" else "Set Event Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Alarm / Reminder Title") },
                    placeholder = { Text("e.g. Christmas Morning Wakeup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Pickers: Hour, Minute, AM/PM
                Text("Select Time:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker (1 to 12)
                    var hourExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = hourExpanded,
                        onExpandedChange = { hourExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        OutlinedTextField(
                            value = displayHour.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hour") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hourExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = hourExpanded,
                            onDismissRequest = { hourExpanded = false }
                        ) {
                            (1..12).forEach { h ->
                                DropdownMenuItem(
                                    text = { Text(h.toString()) },
                                    onClick = {
                                        val actualHour = if (isPm) {
                                            if (h == 12) 12 else h + 12
                                        } else {
                                            if (h == 12) 0 else h
                                        }
                                        hour = actualHour
                                        hourExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Minute picker (00, 05, 10, 15... 55)
                    var minuteExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = minuteExpanded,
                        onExpandedChange = { minuteExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = String.format(Locale.US, "%02d", minute),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Min") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minuteExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = minuteExpanded,
                            onDismissRequest = { minuteExpanded = false }
                        ) {
                            listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(String.format(Locale.US, "%02d", m)) },
                                    onClick = {
                                        minute = m
                                        minuteExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // AM / PM Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                isPm = !isPm
                                val h12 = if (hour == 0 || hour == 12) 12 else hour % 12
                                hour = if (isPm) (if (h12 == 12) 12 else h12 + 12) else (if (h12 == 12) 0 else h12)
                            }
                    ) {
                        Text(
                            text = if (isPm) "PM" else "AM",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HolidayCrimson
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }

                // Repeat Mode Selector
                Text("Repeat:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(repeatModes) { mode ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (repeatMode == mode) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { repeatMode = mode }
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (repeatMode == mode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Wake up kids, open stockings") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Please enter title"
                    } else {
                        onSave(title.trim(), hour, minute, type, repeatMode, notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "ALARM") HolidayCrimson else HolidayPineGreen
                )
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val isPm = hour >= 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (isPm) "PM" else "AM"
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
}
