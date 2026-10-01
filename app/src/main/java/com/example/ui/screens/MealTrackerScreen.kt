package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MealEntity
import com.example.data.repository.TrackersRepository
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTrackerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { TrackersRepository(context) }
    val scope = rememberCoroutineScope()

    var currentDateCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    val currentDateString = remember(currentDateCalendar) { dateFormatter.format(currentDateCalendar.time) }
    val displayDateString = remember(currentDateCalendar) { displayDateFormatter.format(currentDateCalendar.time) }

    val isToday = remember(currentDateCalendar) {
        val today = Calendar.getInstance()
        today.get(Calendar.YEAR) == currentDateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == currentDateCalendar.get(Calendar.DAY_OF_YEAR)
    }

    val dailyMeals by repository.getMealsForDate(currentDateString).collectAsState(initial = emptyList())
    val totalCalories by repository.getTotalCaloriesForDate(currentDateString).collectAsState(initial = 0)

    var showAddDialog by remember { mutableStateOf(false) }

    val festivePresets = remember {
        listOf(
            Triple("Gingerbread Cookies", 180, "Festive Treat"),
            Triple("Hot Cocoa & Marshmallows", 220, "Festive Snack"),
            Triple("Roasted Holiday Turkey", 420, "Dinner"),
            Triple("Christmas Eggnog", 240, "Festive Treat"),
            Triple("Festive Fruitcake", 290, "Festive Snack"),
            Triple("Peppermint Candy", 60, "Festive Treat")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFD4AF37),
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_meal_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meal")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Switcher Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newCal = currentDateCalendar.clone() as Calendar
                            newCal.add(Calendar.DAY_OF_MONTH, -1)
                            currentDateCalendar = newCal
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isToday) "Today" else displayDateString,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isToday) {
                            Text(
                                text = displayDateString,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val newCal = currentDateCalendar.clone() as Calendar
                            newCal.add(Calendar.DAY_OF_MONTH, 1)
                            currentDateCalendar = newCal
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }

            // Summary Card
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
                                colors = listOf(Color(0xFF8B4513), Color(0xFFD2691E), Color(0xFFD4AF37))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Feast & Meals",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.9f))
                            )
                            Text(
                                text = "${totalCalories ?: 0} kcal",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }

                        val festiveCount = dailyMeals.count { it.isFestiveSpecial }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$festiveCount Festive",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${dailyMeals.size} logged",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }
                }
            }

            // Quick Add Festive Delicacy Row
            Text(
                text = "Quick Holiday Treats",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(festivePresets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    repository.saveMeal(
                                        MealEntity(
                                            dateString = currentDateString,
                                            mealType = preset.third,
                                            foodName = preset.first,
                                            calories = preset.second,
                                            isFestiveSpecial = true
                                        )
                                    )
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${preset.first} (${preset.second}k)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Meals List
            if (dailyMeals.isEmpty()) {
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
                        Text(text = "🍽️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No meals logged for this day",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Log your breakfast, holiday feasts, festive baking, and dinner treats to keep track of your daily holiday delights.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2691E))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Meal or Treat")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dailyMeals, key = { it.id }) { meal ->
                        MealItemCard(
                            meal = meal,
                            onDelete = {
                                scope.launch {
                                    repository.deleteMeal(meal.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMealDialog(
            currentDateString = currentDateString,
            onDismiss = { showAddDialog = false },
            onSave = { type, foodName, calories, isFestive, notes ->
                scope.launch {
                    repository.saveMeal(
                        MealEntity(
                            dateString = currentDateString,
                            mealType = type,
                            foodName = foodName,
                            calories = calories,
                            notes = notes,
                            isFestiveSpecial = isFestive
                        )
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun MealItemCard(
    meal: MealEntity,
    onDelete: () -> Unit
) {
    val mealIcon: ImageVector = when (meal.mealType) {
        "Breakfast" -> Icons.Default.Coffee
        "Lunch" -> Icons.Default.LunchDining
        "Dinner" -> Icons.Default.DinnerDining
        "Holiday Feast" -> Icons.Default.Restaurant
        else -> Icons.Default.Cookie
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (meal.isFestiveSpecial) 1.5.dp else 1.dp,
            color = if (meal.isFestiveSpecial) HolidayGold else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (meal.isFestiveSpecial) HolidayGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mealIcon,
                    contentDescription = null,
                    tint = if (meal.isFestiveSpecial) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = meal.foodName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (meal.isFestiveSpecial) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "✨", fontSize = 14.sp)
                    }
                }

                Text(
                    text = meal.mealType + (if (!meal.notes.isNullOrEmpty()) " • ${meal.notes}" else ""),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                if (meal.calories != null && meal.calories > 0) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealDialog(
    currentDateString: String,
    onDismiss: () -> Unit,
    onSave: (mealType: String, foodName: String, calories: Int?, isFestive: Boolean, notes: String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Breakfast") }
    var caloriesStr by remember { mutableStateOf("") }
    var isFestive by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Holiday Feast", "Festive Snack", "Dessert")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Meal / Feast", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it; error = null },
                    label = { Text("Food / Recipe Name") },
                    placeholder = { Text("e.g. Gingerbread Pancakes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = mealType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        mealTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    mealType = type
                                    if (type == "Holiday Feast" || type == "Festive Snack") {
                                        isFestive = true
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) caloriesStr = it },
                    label = { Text("Estimated Calories (Optional)") },
                    placeholder = { Text("e.g. 350") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFestive = !isFestive }
                ) {
                    Checkbox(
                        checked = isFestive,
                        onCheckedChange = { isFestive = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Mark as Festive Holiday Special ✨", style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Ingredients (Optional)") },
                    singleLine = true,
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
                    if (foodName.isBlank()) {
                        error = "Please enter food name"
                    } else {
                        val calories = caloriesStr.toIntOrNull()
                        onSave(mealType, foodName.trim(), calories, isFestive, notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2691E))
            ) {
                Text("Save Meal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
