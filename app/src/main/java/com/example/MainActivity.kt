package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.HolidayTopAppBar
import com.example.ui.navigation.HolidayBottomBar
import com.example.ui.navigation.HolidayNavHost
import com.example.ui.navigation.Screen
import com.example.ui.theme.HolidayWishesTheme
import com.example.ui.viewmodel.HolidayViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolidayWishesTheme {
                HolidayWishesApp()
            }
        }
    }
}

@Composable
fun HolidayWishesApp(
    viewModel: HolidayViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val title = when (currentRoute) {
        Screen.Home.route -> "Holiday Wishes"
        Screen.Wishes.route -> "Wishes Collection"
        Screen.CreateWish.route -> "AI Wish Generator"
        Screen.AiWishGenerator.route -> "AI Wish Generator"
        Screen.AiHistory.route -> "My AI Wishes"
        Screen.Cards.route -> "Holiday Cards"
        Screen.CustomizeCard.route -> "Card Creator"
        Screen.MyCards.route -> "My Saved Cards"
        Screen.Favorites.route -> "Saved Favorites"
        Screen.Settings.route -> "Settings"
        else -> "Holiday Wishes"
    }

    // Hide top bar on fullscreen card editor and my cards screen to maximize workspace
    val hideTopBar = currentRoute == Screen.CustomizeCard.route || currentRoute == Screen.MyCards.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!hideTopBar) {
                HolidayTopAppBar(title = title)
            }
        },
        bottomBar = {
            HolidayBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        HolidayNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
