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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.data.firebase.FirestoreManager
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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
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

            // 1. Home
            DrawerNavItem(
                icon = Icons.Default.Home,
                title = "Home",
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigateToRoute(Screen.Home.route) },
                iconTint = HolidayCrimson
            )

            // 2. Ai wish generator
            DrawerNavItem(
                icon = Icons.Default.AutoAwesome,
                title = "Ai wish generator",
                isSelected = currentRoute == Screen.AiWishGenerator.route || currentRoute == Screen.CreateWish.route,
                onClick = { onNavigateToRoute(Screen.AiWishGenerator.route) },
                iconTint = HolidayGold
            )

            // 3. Wishes library
            DrawerNavItem(
                icon = Icons.AutoMirrored.Filled.Comment,
                title = "Wishes library",
                isSelected = currentRoute == Screen.Wishes.route,
                onClick = { onNavigateToRoute(Screen.Wishes.route) },
                iconTint = HolidayCrimson
            )

            // 4. Card library
            DrawerNavItem(
                icon = Icons.Default.CardGiftcard,
                title = "Card library",
                isSelected = currentRoute == Screen.Cards.route,
                onClick = { onNavigateToRoute(Screen.Cards.route) },
                iconTint = HolidayCrimson
            )

            // 5. Template
            DrawerNavItem(
                icon = Icons.Default.PhotoAlbum,
                title = "Template",
                isSelected = currentRoute == Screen.CustomizeCard.route,
                onClick = { onNavigateToRoute(Screen.CustomizeCard.route) },
                iconTint = Color(0xFF00D2FC)
            )

            // 6. Save cards and wishes
            DrawerNavItem(
                icon = Icons.Default.Favorite,
                title = "Save cards and wishes",
                isSelected = currentRoute == Screen.MyCards.route || currentRoute == Screen.Favorites.route,
                onClick = { onNavigateToRoute(Screen.MyCards.route) },
                iconTint = HolidayCrimson
            )

            // 7. Birthday tracker and countdown
            DrawerNavItem(
                icon = Icons.Default.Cake,
                title = "Birthday tracker and countdown",
                isSelected = currentRoute == Screen.BirthdayTracker.route,
                onClick = { onNavigateToRoute(Screen.BirthdayTracker.route) },
                iconTint = HolidayCrimson
            )

            // 8. Season earning tracker
            DrawerNavItem(
                icon = Icons.Default.AttachMoney,
                title = "Season earning tracker",
                isSelected = currentRoute == Screen.SeasonEarnings.route,
                onClick = { onNavigateToRoute(Screen.SeasonEarnings.route) },
                iconTint = HolidayPineGreen
            )

            // 9. Expenses tracker
            DrawerNavItem(
                icon = Icons.Default.ReceiptLong,
                title = "Expenses tracker",
                isSelected = currentRoute == Screen.ExpensesTracker.route,
                onClick = { onNavigateToRoute(Screen.ExpensesTracker.route) },
                iconTint = Color(0xFFE53935)
            )

            // 10. Daily meal tracker
            DrawerNavItem(
                icon = Icons.Default.Restaurant,
                title = "Daily meal tracker",
                isSelected = currentRoute == Screen.MealTracker.route,
                onClick = { onNavigateToRoute(Screen.MealTracker.route) },
                iconTint = Color(0xFFD2691E)
            )

            // 11. Schedule delivery
            DrawerNavItem(
                icon = Icons.Default.Send,
                title = "Schedule delivery",
                isSelected = currentRoute == Screen.ScheduleDelivery.route,
                onClick = { onNavigateToRoute(Screen.ScheduleDelivery.route) },
                iconTint = Color(0xFF1E88E5)
            )

            // 12. Alarm and reminder
            DrawerNavItem(
                icon = Icons.Default.Alarm,
                title = "Alarm and reminder",
                isSelected = currentRoute == Screen.AlarmReminder.route,
                onClick = { onNavigateToRoute(Screen.AlarmReminder.route) },
                iconTint = Color(0xFFE65100)
            )

            DrawerDivider()

            // 3 Account & Support items
            DrawerSectionHeader(title = "ACCOUNT & SUPPORT")

            DrawerNavItem(
                icon = Icons.Default.Star,
                title = "Holiday Wishes Pro",
                isSelected = currentRoute == Screen.ProSubscription.route,
                onClick = { onNavigateToRoute(Screen.ProSubscription.route) },
                iconTint = HolidayGold
            )

            DrawerNavItem(
                icon = Icons.Default.SupportAgent,
                title = "Customer Support",
                isSelected = currentRoute == Screen.CustomerCenter.route,
                onClick = { onNavigateToRoute(Screen.CustomerCenter.route) },
                iconTint = Color(0xFF1976D2)
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
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
