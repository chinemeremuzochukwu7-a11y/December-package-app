package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.HolidayNavigationDrawer
import com.example.ui.components.HolidayTopAppBar
import com.example.ui.components.NotificationPermissionPrompt
import com.example.ui.navigation.HolidayBottomBar
import com.example.ui.navigation.HolidayNavHost
import com.example.ui.navigation.Screen
import com.example.ui.theme.HolidayWishesTheme
import com.example.ui.viewmodel.HolidayViewModel
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Prompt user on app open to turn on notifications for alarms, countdowns, and wishes
    NotificationPermissionPrompt(
        onPermissionGranted = {
            if (!viewModel.notificationsEnabled.value) {
                viewModel.toggleNotifications()
            }
        }
    )

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val title = when (currentRoute) {
        Screen.Home.route -> "Holiday Wishes"
        Screen.Wishes.route -> "Wishes Collection"
        Screen.CreateWish.route -> "AI Wish Generator"
        Screen.AiWishGenerator.route -> "AI Wish Generator"
        Screen.BirthdayTracker.route -> "Birthday Tracker"
        Screen.ExpensesTracker.route -> "Expenses Tracker"
        Screen.MealTracker.route -> "Daily Meal Tracker"
        Screen.AlarmReminder.route -> "Alarms & Reminders"
        Screen.AiHistory.route -> "My AI Wishes"
        Screen.Cards.route -> "Holiday Cards"
        Screen.CustomizeCard.route -> "Card Creator"
        Screen.MyCards.route -> "My Saved Cards"
        Screen.Favorites.route -> "Saved Favorites"
        Screen.Settings.route -> "Settings"
        Screen.ProSubscription.route -> "Holiday Wishes Pro"
        Screen.CustomerCenter.route -> "Customer Center"
        else -> "Holiday Wishes"
    }

    // Hide top bar on fullscreen card editor, my cards screen, and paywall screen
    val hideTopBar = currentRoute == Screen.CustomizeCard.route || 
                     currentRoute == Screen.MyCards.route ||
                     currentRoute == Screen.ProSubscription.route ||
                     currentRoute == Screen.CustomerCenter.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HolidayNavigationDrawer(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    scope.launch { drawerState.close() }
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
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!hideTopBar) {
                    HolidayTopAppBar(
                        title = title,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onProClick = {
                            navController.navigate(Screen.ProSubscription.route) {
                                launchSingleTop = true
                            }
                        }
                    )
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
}
