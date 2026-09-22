package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class CountdownTarget(val label: String, val icon: String, val month: Int, val day: Int) {
    CHRISTMAS("Christmas Eve & Day", "🎄", Calendar.DECEMBER, 25),
    NEW_YEAR("New Year 2027", "🎆", Calendar.JANUARY, 1)
}

@Composable
fun HolidayCountdownCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTarget by remember { mutableIntStateOf(0) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val currentTarget = if (selectedTarget == 0) CountdownTarget.CHRISTMAS else CountdownTarget.NEW_YEAR

    // Calculate time left until target
    val now = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    val currentYear = now.get(Calendar.YEAR)

    val targetCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentTarget.month)
        set(Calendar.DAY_OF_MONTH, currentTarget.day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (now.after(targetCal)) {
        targetCal.add(Calendar.YEAR, 1)
    }

    val diffMillis = (targetCal.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1215)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A151C),
                            Color(0xFF140D10)
                        )
                    )
                )
                .border(
                    border = androidx.compose.foundation.BorderStroke(1.dp, HolidayGold.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top switcher row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        Surface(
                            color = if (selectedTarget == 0) HolidayCrimson else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.clickable { selectedTarget = 0 }
                        ) {
                            Text(
                                text = "🎄 Christmas",
                                color = if (selectedTarget == 0) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = if (selectedTarget == 1) HolidayGold else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.clickable { selectedTarget = 1 }
                        ) {
                            Text(
                                text = "🎆 New Year",
                                color = if (selectedTarget == 1) Color.Black else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Notification / Remind Me Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(HolidayGold.copy(alpha = 0.15f))
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "✨ Reminder set! You'll be ready to send heartfelt wishes on ${currentTarget.label}.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Remind Me",
                            tint = HolidayGold,
                            modifier = Modifier.padding(end = 4.dp).height(14.dp)
                        )
                        Text(
                            text = "Remind Me",
                            color = HolidayGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "Countdown to ${currentTarget.label}",
                    fontFamily = PlayfairDisplayFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Digit blocks (Days, Hours, Minutes, Seconds)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CountdownUnitBlock(value = days.toString().padStart(2, '0'), unit = "DAYS")
                    CountdownUnitBlock(value = hours.toString().padStart(2, '0'), unit = "HOURS")
                    CountdownUnitBlock(value = minutes.toString().padStart(2, '0'), unit = "MINS")
                    CountdownUnitBlock(value = seconds.toString().padStart(2, '0'), unit = "SECS", isAccent = true)
                }
            }
        }
    }
}

@Composable
private fun CountdownUnitBlock(
    value: String,
    unit: String,
    isAccent: Boolean = false
) {
    Surface(
        color = if (isAccent) HolidayGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isAccent) HolidayGold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = Modifier.width(68.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAccent) HolidayGold else Color.White
            )
            Text(
                text = unit,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}
