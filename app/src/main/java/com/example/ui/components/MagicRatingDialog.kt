package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.utils.RateAppManager

@Composable
fun MagicRatingDialog(
    modifier: Modifier = Modifier
) {
    val showDialog by RateAppManager.showMagicRatingDialog.collectAsState()
    val eventTitle by RateAppManager.magicEventTitle.collectAsState()
    val context = LocalContext.current

    if (showDialog) {
        var selectedStars by remember { mutableIntStateOf(5) }

        AlertDialog(
            onDismissRequest = { RateAppManager.dismissMagicRatingDialog(context, neverShowAgain = false) },
            modifier = modifier.testTag("magic_rating_dialog"),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = HolidayGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "✨", fontSize = 32.sp)
                    }
                }
            },
            title = {
                Text(
                    text = eventTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enjoying the magic of Holiday Wishes? Please take a moment to rate us on Google Play. Your review supports new festive templates and free AI credits!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5 Star rating selector
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= selectedStars
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$i stars",
                                tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(horizontal = 2.dp)
                                    .clickable { selectedStars = i }
                                    .testTag("rating_star_$i")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (selectedStars) {
                            5 -> "🌟 5 Stars - Outstanding Experience!"
                            4 -> "✨ 4 Stars - Very Good!"
                            3 -> "👍 3 Stars - Good!"
                            else -> "We will keep improving for you!"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedStars >= 4) HolidayGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        RateAppManager.launchGooglePlayReview(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_on_google_play_button")
                ) {
                    Text(
                        text = "Rate on Google Play 🌟",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { RateAppManager.dismissMagicRatingDialog(context, neverShowAgain = true) },
                        modifier = Modifier.testTag("dont_ask_again_button")
                    ) {
                        Text("Never", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    TextButton(
                        onClick = { RateAppManager.dismissMagicRatingDialog(context, neverShowAgain = false) },
                        modifier = Modifier.testTag("remind_me_later_button")
                    ) {
                        Text("Maybe Later", fontWeight = FontWeight.SemiBold, color = HolidayCrimson)
                    }
                }
            }
        )
    }
}
