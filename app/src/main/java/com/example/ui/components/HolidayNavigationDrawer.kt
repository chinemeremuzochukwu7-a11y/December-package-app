package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.subscription.SubscriptionManager
import com.example.ui.navigation.Screen
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayCrimsonContainer
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen

@Composable
fun HolidayNavigationDrawer(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPro by SubscriptionManager.isPro.collectAsState()
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .testTag("holiday_drawer_sheet"),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF8B0000), Color(0xFFC41E3A), Color(0xFFD4AF37))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Holiday Wishes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Festive & Daily Hub",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPro) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onNavigateToRoute(Screen.ProSubscription.route) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPro) "👑 PRO MEMBER" else "✨ Upgrade to Pro",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPro) Color.Black else Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Trackers & Daily Planners (Top Highlight)
            DrawerSectionHeader(title = "DAILY & CELEBRATION TRACKERS")

            DrawerNavItem(
                icon = Icons.Default.Cake,
                title = "Birthday Tracker",
                badge = "Countdown",
                isSelected = currentRoute == Screen.BirthdayTracker.route,
                onClick = { onNavigateToRoute(Screen.BirthdayTracker.route) },
                iconTint = HolidayCrimson
            )

            DrawerNavItem(
                icon = Icons.Default.AttachMoney,
                title = "Expenses Tracker",
                badge = "Budget",
                isSelected = currentRoute == Screen.ExpensesTracker.route,
                onClick = { onNavigateToRoute(Screen.ExpensesTracker.route) },
                iconTint = HolidayPineGreen
            )

            DrawerNavItem(
                icon = Icons.Default.Restaurant,
                title = "Daily Meal Tracker",
                badge = "Feasts",
                isSelected = currentRoute == Screen.MealTracker.route,
                onClick = { onNavigateToRoute(Screen.MealTracker.route) },
                iconTint = Color(0xFFD2691E)
            )

            DrawerNavItem(
                icon = Icons.Default.Alarm,
                title = "Alarms & Reminders",
                badge = "Sound",
                isSelected = currentRoute == Screen.AlarmReminder.route,
                onClick = { onNavigateToRoute(Screen.AlarmReminder.route) },
                iconTint = Color(0xFFE65100)
            )

            DrawerDivider()

            // Section 2: Festive & AI Creations
            DrawerSectionHeader(title = "FESTIVE CREATOR & WISHES")

            DrawerNavItem(
                icon = Icons.Default.Home,
                title = "Home",
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigateToRoute(Screen.Home.route) }
            )

            DrawerNavItem(
                icon = Icons.Default.AutoAwesome,
                title = "AI Wish Generator",
                badge = "AI",
                isSelected = currentRoute == Screen.AiWishGenerator.route,
                onClick = { onNavigateToRoute(Screen.AiWishGenerator.route) },
                iconTint = HolidayGold
            )

            DrawerNavItem(
                icon = Icons.AutoMirrored.Filled.Comment,
                title = "Wishes Library",
                isSelected = currentRoute == Screen.Wishes.route,
                onClick = { onNavigateToRoute(Screen.Wishes.route) }
            )

            DrawerNavItem(
                icon = Icons.Default.CardGiftcard,
                title = "Holiday Cards Studio",
                isSelected = currentRoute == Screen.Cards.route,
                onClick = { onNavigateToRoute(Screen.Cards.route) }
            )

            DrawerNavItem(
                icon = Icons.Default.PhotoAlbum,
                title = "My Saved Cards",
                isSelected = currentRoute == Screen.MyCards.route,
                onClick = { onNavigateToRoute(Screen.MyCards.route) }
            )

            DrawerNavItem(
                icon = Icons.Default.Favorite,
                title = "Saved Favorites",
                isSelected = currentRoute == Screen.Favorites.route,
                onClick = { onNavigateToRoute(Screen.Favorites.route) },
                iconTint = HolidayCrimson
            )

            DrawerDivider()

            // Section 3: Settings & Support
            DrawerSectionHeader(title = "ACCOUNT & SUPPORT")

            DrawerNavItem(
                icon = Icons.Default.Star,
                title = "Holiday Wishes Pro",
                badge = if (isPro) "Active" else "Upgrade",
                isSelected = currentRoute == Screen.ProSubscription.route,
                onClick = { onNavigateToRoute(Screen.ProSubscription.route) },
                iconTint = HolidayGold
            )

            DrawerNavItem(
                icon = Icons.Default.SupportAgent,
                title = "Customer Center",
                isSelected = currentRoute == Screen.CustomerCenter.route,
                onClick = { onNavigateToRoute(Screen.CustomerCenter.route) }
            )

            DrawerNavItem(
                icon = Icons.Default.Settings,
                title = "Settings & Preferences",
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigateToRoute(Screen.Settings.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconTint: Color? = null
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) HolidayCrimson else (iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant)
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                badge?.let {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = HolidayCrimsonContainer.copy(alpha = 0.7f),
            selectedTextColor = HolidayCrimson
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("drawer_nav_${title.replace(" ", "_").lowercase()}")
    )
}

@Composable
private fun DrawerDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
