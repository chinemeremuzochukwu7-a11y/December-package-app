package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Wishes : Screen(
        route = "wishes",
        title = "Wishes",
        selectedIcon = Icons.AutoMirrored.Filled.Comment,
        unselectedIcon = Icons.AutoMirrored.Filled.Comment
    )

    object CreateWish : Screen(
        route = "create_wish",
        title = "Create",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    )

    object AiWishGenerator : Screen(
        route = "ai_wish_generator",
        title = "AI Wishes",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    object BirthdayTracker : Screen(
        route = "birthday_tracker",
        title = "Birthdays",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake
    )

    object ExpensesTracker : Screen(
        route = "expenses_tracker",
        title = "Expenses",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney
    )

    object MealTracker : Screen(
        route = "meal_tracker",
        title = "Meals",
        selectedIcon = Icons.Filled.Restaurant,
        unselectedIcon = Icons.Outlined.Restaurant
    )

    object AlarmReminder : Screen(
        route = "alarm_reminder",
        title = "Alarms & Reminders",
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm
    )

    object AiHistory : Screen(
        route = "ai_history",
        title = "My AI Wishes",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    object Cards : Screen(
        route = "cards",
        title = "Cards",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    )

    object CustomizeCard : Screen(
        route = "customize_card",
        title = "Customize Card",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    )

    object MyCards : Screen(
        route = "my_cards",
        title = "My Cards",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    )

    object Favorites : Screen(
        route = "favorites",
        title = "Favorites",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object ProSubscription : Screen(
        route = "pro_subscription",
        title = "Holiday Wishes Pro",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    )

    object CustomerCenter : Screen(
        route = "customer_center",
        title = "Customer Center",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

// Bottom nav items: Home, AI Wishes, Birthdays, Expenses, Meals
val BottomNavItems = listOf(
    Screen.Home,
    Screen.AiWishGenerator,
    Screen.BirthdayTracker,
    Screen.ExpensesTracker,
    Screen.MealTracker
)
