package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.subscription.SubscriptionManager
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayTopAppBar(
    title: String = "Holiday Wishes",
    subtitle: String? = null,
    onMenuClick: () -> Unit = {},
    onProClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPro by SubscriptionManager.isPro.collectAsState()
    val credits by SubscriptionManager.credits.collectAsState()
    val currentUser by com.example.data.firebase.AuthManager.currentUser.collectAsState()

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("side_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Side Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = HolidayCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.testTag("app_bar_title")
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Credits Chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HolidayGold.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HolidayGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clickable(onClick = onProClick)
                        .testTag("app_bar_credits_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp)
                    ) {
                        Text(text = "🪙", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.5.dp))
                        Text(
                            text = "$credits",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HolidayGold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Pro Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPro) Color(0xFFFFD700) else HolidayCrimson.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPro) Color(0xFFFFD700) else HolidayCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clickable(onClick = onProClick)
                        .testTag("pro_badge_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = if (isPro) "👑 PRO" else "★ PRO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPro) Color.Black else HolidayCrimson
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Google Account Icon/Badge
                Surface(
                    shape = CircleShape,
                    color = if (currentUser != null) HolidayCrimson.copy(alpha = 0.15f) else Color(0xFFE0E0E0),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, if (currentUser != null) HolidayCrimson else Color(0xFFBDBDBD)),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onAccountClick)
                        .testTag("app_bar_account_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (currentUser != null) {
                            val initial = (currentUser?.displayName?.take(1) ?: currentUser?.email?.take(1) ?: "G").uppercase()
                            Text(
                                text = initial,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HolidayCrimson
                            )
                        } else {
                            Text(
                                text = "G",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    )
}
