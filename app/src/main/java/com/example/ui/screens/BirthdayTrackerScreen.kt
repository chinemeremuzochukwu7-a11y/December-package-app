package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BirthdayEntity
import com.example.data.repository.TrackersRepository
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayTrackerScreen(
    onNavigateToAiWishForPerson: (name: String, relationship: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { TrackersRepository(context) }
    val scope = rememberCoroutineScope()

    val allBirthdays by repository.getAllBirthdays().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterMonth by remember { mutableIntStateOf(0) } // 0 = all months

    val sortedBirthdays = remember(allBirthdays, searchQuery, selectedFilterMonth) {
        allBirthdays
            .filter { birthday ->
                (searchQuery.isEmpty() || birthday.name.contains(searchQuery, ignoreCase = true) ||
                        birthday.giftIdeas.contains(searchQuery, ignoreCase = true)) &&
                        (selectedFilterMonth == 0 || birthday.birthMonth == selectedFilterMonth)
            }
            .sortedBy { TrackersRepository.daysUntilBirthday(it.birthMonth, it.birthDay) }
    }

    val upcomingCount = sortedBirthdays.count { TrackersRepository.daysUntilBirthday(it.birthMonth, it.birthDay) <= 30 }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HolidayCrimson,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_birthday_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Birthday")
            }
        },
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
                                colors = listOf(Color(0xFF8B0000), Color(0xFFC41E3A), Color(0xFFD4AF37))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎂", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Birthday Tracker",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (upcomingCount > 0)
                                    "$upcomingCount birthday${if (upcomingCount > 1) "s" else ""} coming in 30 days"
                                else
                                    "Never miss celebrating a loved one's special day",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Search & Filter Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or gift idea...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Month Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterMonthChip(
                        label = "All Months",
                        isSelected = selectedFilterMonth == 0,
                        onClick = { selectedFilterMonth = 0 }
                    )
                }
                val monthNames = DateFormatSymbols().shortMonths
                for (m in 1..12) {
                    item {
                        FilterMonthChip(
                            label = monthNames[m - 1],
                            isSelected = selectedFilterMonth == m,
                            onClick = { selectedFilterMonth = m }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Birthdays List
            if (sortedBirthdays.isEmpty()) {
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
                        Text(text = "🎈", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedFilterMonth != 0)
                                "No birthdays match your filter"
                            else
                                "No birthdays tracked yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your family, friends, and colleagues to receive count-down reminders and send AI wishes.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Birthday")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sortedBirthdays, key = { it.id }) { birthday ->
                        val daysUntil = TrackersRepository.daysUntilBirthday(birthday.birthMonth, birthday.birthDay)
                        val turningAge = TrackersRepository.calculateTurningAge(birthday.birthYear, birthday.birthMonth, birthday.birthDay)

                        BirthdayCardItem(
                            birthday = birthday,
                            daysUntil = daysUntil,
                            turningAge = turningAge,
                            onAiWishClick = {
                                onNavigateToAiWishForPerson(birthday.name, birthday.relationship)
                            },
                            onDeleteClick = {
                                scope.launch {
                                    repository.deleteBirthday(birthday.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBirthdayDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, month, day, year, relationship, giftIdeas ->
                scope.launch {
                    repository.saveBirthday(
                        BirthdayEntity(
                            name = name,
                            birthMonth = month,
                            birthDay = day,
                            birthYear = year,
                            relationship = relationship,
                            giftIdeas = giftIdeas
                        )
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun FilterMonthChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun BirthdayCardItem(
    birthday: BirthdayEntity,
    daysUntil: Int,
    turningAge: Int?,
    onAiWishClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val monthNames = DateFormatSymbols().shortMonths
    val monthStr = if (birthday.birthMonth in 1..12) monthNames[birthday.birthMonth - 1] else ""
    val isToday = daysUntil == 0
    val isSoon = daysUntil in 1..7

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                HolidayCrimson.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isToday) 2.dp else 1.dp,
            color = if (isToday) HolidayGold else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("birthday_item_${birthday.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Days Countdown badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday) HolidayGold else HolidayPineGreen.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isToday) "🎉" else "🎂",
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = birthday.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = birthday.relationship,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "$monthStr ${birthday.birthDay}" + (turningAge?.let { " • Turning $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Days until badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isToday -> HolidayGold
                        isSoon -> HolidayCrimson
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isToday -> "TODAY!"
                            daysUntil == 1 -> "Tomorrow"
                            else -> "In $daysUntil days"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isToday -> Color.Black
                                isSoon -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (birthday.giftIdeas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = HolidayCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gift idea: ${birthday.giftIdeas}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAiWishClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = HolidayGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Birthday Wish", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(34.dp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBirthdayDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, month: Int, day: Int, year: Int?, relationship: String, giftIdeas: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var month by remember { mutableIntStateOf(1) }
    var day by remember { mutableIntStateOf(1) }
    var yearStr by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Friend") }
    var giftIdeas by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val relationships = listOf("Friend", "Family", "Partner", "Parent", "Child", "Colleague", "Loved One")
    val monthNames = DateFormatSymbols().months

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎂", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Birthday Reminder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Person's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Month Picker
                    var monthExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it },
                        modifier = Modifier.weight(1.3f)
                    ) {
                        OutlinedTextField(
                            value = monthNames[month - 1],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            monthNames.take(12).forEachIndexed { index, mName ->
                                DropdownMenuItem(
                                    text = { Text(mName) },
                                    onClick = {
                                        month = index + 1
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Day Picker
                    var dayExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = it },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        OutlinedTextField(
                            value = day.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            (1..31).forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.toString()) },
                                    onClick = {
                                        day = d
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Optional Birth Year
                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) yearStr = it },
                    label = { Text("Birth Year (Optional)") },
                    placeholder = { Text("e.g. 1996") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Relationship selector
                Text(text = "Relationship:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(relationships) { rel ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (relationship == rel) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { relationship = rel }
                        ) {
                            Text(
                                text = rel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (relationship == rel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Gift ideas
                OutlinedTextField(
                    value = giftIdeas,
                    onValueChange = { giftIdeas = it },
                    label = { Text("Gift Ideas or Notes") },
                    placeholder = { Text("e.g. loves books, coffee beans...") },
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
                    if (name.isBlank()) {
                        error = "Please enter a name"
                    } else {
                        val year = yearStr.toIntOrNull()
                        onSave(name.trim(), month, day, year, relationship, giftIdeas.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
