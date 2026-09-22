package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
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
        title = "AI Wish Generator",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
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
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Wishes,
    Screen.Cards,
    Screen.Favorites,
    Screen.Settings
)
