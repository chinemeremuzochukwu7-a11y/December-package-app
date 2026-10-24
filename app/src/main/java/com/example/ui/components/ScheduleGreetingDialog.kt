package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduledGreetingEntity
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.utils.ScheduledGreetingScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleGreetingDialog(
    greetingTitle: String,
    initialRecipientName: String = "",
    messageText: String,
    cardImagePath: String? = null,
    onDismiss: () -> Unit,
    onScheduledSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPlatform by remember { mutableStateOf("WhatsApp") }
    var recipientName by remember { mutableStateOf(initialRecipientName.ifBlank { "Loved One" }) }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }

    // Contact Picker Launcher from Device Address Book
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        if (contactUri != null) {
            try {
                val cursor = context.contentResolver.query(contactUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = it.getString(nameIndex)
                            if (!name.isNullOrBlank()) recipientName = name
                        }
                        val idIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                        if (idIndex != -1) {
                            val contactId = it.getString(idIndex)
                            val phoneCursor = context.contentResolver.query(
                                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pCursor ->
                                if (pCursor.moveToFirst()) {
                                    val numIndex = pCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numIndex != -1) {
                                        val num = pCursor.getString(numIndex)
                                        if (!num.isNullOrBlank()) phoneNumber = num
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val platformOptions = remember {
        listOf(
            Triple("WhatsApp", "💬 WhatsApp", "Direct chat or WhatsApp contact"),
            Triple("Telegram", "✈️ Telegram", "Telegram messaging"),
            Triple("Instagram", "📸 Instagram", "Direct message / Story"),
            Triple("Messenger", "📘 Messenger", "Facebook Messenger"),
            Triple("Twitter", "🐦 X (Twitter)", "Post or direct message"),
            Triple("SMS", "✉️ SMS Messages", "Native cellular messaging"),
            Triple("Email", "📧 Email / Gmail", "Holiday email greetings"),
            Triple("Snapchat", "👻 Snapchat", "Snapchat share"),
            Triple("Other Apps", "📤 Other Apps", "All installed social media & apps")
        )
    }

    // Target Calendar (defaults to tomorrow 9:00 AM or 1 hour from now)
    val now = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY) + 1) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    // Normalize if hour overflowed
    if (selectedHour >= 24) {
        selectedHour = 9
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        selectedYear = cal.get(Calendar.YEAR)
        selectedMonth = cal.get(Calendar.MONTH)
        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
    }

    val targetCalendar = remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp)
            .testTag("schedule_greeting_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏰", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Schedule Greeting Delivery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select Platform & Person to Auto-Deliver",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Step 1: Select Platform
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Step 1: Select Delivery Platform",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HolidayCrimson
                        )
                        Text(
                            text = "Choose which app will deliver your scheduled greeting",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            platformOptions.forEach { (platId, platLabel, platDesc) ->
                                val isSelected = selectedPlatform == platId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPlatform = platId },
                                    label = { Text(platLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (platId == "WhatsApp") HolidayPineGreen.copy(alpha = 0.25f) else HolidayCrimson.copy(alpha = 0.2f),
                                        selectedLabelColor = if (platId == "WhatsApp") HolidayPineGreen else HolidayCrimson
                                    ),
                                    modifier = Modifier.testTag("platform_chip_${platId.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // 2. Step 2: Select Person & Contact
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Step 2: Select Recipient Person",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HolidayPineGreen
                                )
                                Text(
                                    text = "Recognized by app for direct scheduled delivery",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Pick Contact Button
                            OutlinedButton(
                                onClick = { contactPickerLauncher.launch(null) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pick Contact", fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name") },
                            placeholder = { Text("e.g. Mom, Best Friend, Alex") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HolidayPineGreen)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_recipient_input")
                        )

                        when (selectedPlatform) {
                            "WhatsApp", "SMS", "Telegram" -> {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text(if (selectedPlatform == "WhatsApp") "Recipient WhatsApp Number" else "Recipient Mobile Phone Number") },
                                    placeholder = { Text("+1 234 567 8900") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = HolidayPineGreen)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("schedule_phone_input")
                                )
                            }
                            "Email" -> {
                                OutlinedTextField(
                                    value = emailAddress,
                                    onValueChange = { emailAddress = it },
                                    label = { Text("Recipient Email Address") },
                                    placeholder = { Text("friend@example.com") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = HolidayCrimson)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("schedule_email_input")
                                )
                            }
                            else -> {}
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "✨ When scheduled time arrives, the app recognizes '$recipientName' and delivers directly via $selectedPlatform.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // 3. Exact Date & Time Picker
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Step 3: Scheduled Date & Time",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Quick Holiday Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetChip("Christmas Eve (Dec 24)") {
                                selectedMonth = Calendar.DECEMBER
                                selectedDay = 24
                                selectedHour = 20
                                selectedMinute = 0
                            }
                            PresetChip("Christmas Morning (Dec 25)") {
                                selectedMonth = Calendar.DECEMBER
                                selectedDay = 25
                                selectedHour = 9
                                selectedMinute = 0
                            }
                            PresetChip("New Year's Eve (Dec 31)") {
                                selectedMonth = Calendar.DECEMBER
                                selectedDay = 31
                                selectedHour = 23
                                selectedMinute = 59
                            }
                            PresetChip("New Year Day (Jan 1)") {
                                selectedYear = now.get(Calendar.YEAR) + (if (now.get(Calendar.MONTH) == Calendar.DECEMBER) 1 else 0)
                                selectedMonth = Calendar.JANUARY
                                selectedDay = 1
                                selectedHour = 9
                                selectedMinute = 0
                            }
                            PresetChip("Tomorrow 9 AM") {
                                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                selectedYear = cal.get(Calendar.YEAR)
                                selectedMonth = cal.get(Calendar.MONTH)
                                selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                selectedHour = 9
                                selectedMinute = 0
                            }
                            PresetChip("In 1 Hour") {
                                val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
                                selectedYear = cal.get(Calendar.YEAR)
                                selectedMonth = cal.get(Calendar.MONTH)
                                selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                selectedMinute = cal.get(Calendar.MINUTE)
                            }
                        }

                        // Pick Date & Time buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            selectedYear = y
                                            selectedMonth = m
                                            selectedDay = d
                                        },
                                        selectedYear,
                                        selectedMonth,
                                        selectedDay
                                    ).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateFormatter.format(targetCalendar.time),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, min ->
                                            selectedHour = h
                                            selectedMinute = min
                                        },
                                        selectedHour,
                                        selectedMinute,
                                        false
                                    ).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.75f)
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = timeFormatter.format(targetCalendar.time),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 4. Message Snippet
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HolidayGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Greeting Preview to $recipientName via $selectedPlatform:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3400)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = messageText.take(160) + if (messageText.length > 160) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val epochMillis = targetCalendar.timeInMillis
                    val contactInfo = if (selectedPlatform == "Email") emailAddress.trim() else phoneNumber.trim()
                    val entity = ScheduledGreetingEntity(
                        title = greetingTitle,
                        recipientName = recipientName.ifBlank { "Holiday Friend" },
                        recipientPhone = contactInfo,
                        messageText = messageText,
                        cardTitle = greetingTitle,
                        cardImagePath = cardImagePath,
                        scheduledTimestamp = epochMillis,
                        targetPlatform = selectedPlatform
                    )

                    scope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(context)
                        val insertedId = db.scheduledGreetingDao().insertGreeting(entity)
                        val fullEntity = entity.copy(id = insertedId)
                        ScheduledGreetingScheduler(context).schedule(fullEntity)

                        withContext(Dispatchers.Main) {
                            val formattedTime = "${dateFormatter.format(targetCalendar.time)} at ${timeFormatter.format(targetCalendar.time)} for $recipientName via $selectedPlatform"
                            onScheduledSuccess(formattedTime)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_schedule_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule for $recipientName", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HolidayCrimson.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, HolidayCrimson.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = HolidayCrimson,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
