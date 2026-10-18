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
import com.example.ui.components.MagicRatingDialog
import com.example.ui.navigation.HolidayBottomBar
import com.example.ui.navigation.HolidayNavHost
import com.example.ui.navigation.Screen
import com.example.ui.theme.HolidayWishesTheme
import com.example.ui.viewmodel.HolidayViewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.data.firebase.UserFirestoreRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolidayWishesTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val userId = androidx.compose.runtime.remember { UserFirestoreRepository.getOrCreateUserId(context) }
                HolidayWishesApp(currentUserId = userId)
            }
        }
    }
}

@Composable
fun HolidayWishesApp(
    currentUserId: String = "",
    viewModel: HolidayViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Synchronize user profile & initial 25 credits with Firestore on app open
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            val repo = UserFirestoreRepository(context)
            repo.ensureUserInitialized(currentUserId)
            repo.observeUserProfile(currentUserId).collect { /* updates SubscriptionManager */ }
        }
    }
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
        Screen.Wishes.route -> "Wishes library"
        Screen.CreateWish.route -> "Ai wish generator"
        Screen.AiWishGenerator.route -> "Ai wish generator"
        Screen.BirthdayTracker.route -> "Birthday tracker and countdown"
        Screen.SeasonEarnings.route -> "Season earning tracker"
        Screen.ExpensesTracker.route -> "Expenses tracker"
        Screen.MealTracker.route -> "Daily meal tracker"
        Screen.ScheduleDelivery.route -> "Schedule delivery"
        Screen.AlarmReminder.route -> "Alarm and reminder"
        Screen.AiHistory.route -> "My AI Wishes"
        Screen.Cards.route -> "Card library"
        Screen.CustomizeCard.route -> "Template"
        Screen.MyCards.route -> "Save cards and wishes"
        Screen.Favorites.route -> "Save cards and wishes"
        Screen.Settings.route -> "Settings & Preferences"
        Screen.ProSubscription.route -> "Holiday Wishes Pro"
        Screen.CustomerCenter.route -> "Customer Support"
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
                    if (route == Screen.Home.route) {
                        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                        if (!popped) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        com.example.ui.navigation.AdNavigationHelper.navigateWithSectionAdCheck(context, route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                        if (route == Screen.Home.route) {
                            val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                            if (!popped) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            com.example.ui.navigation.AdNavigationHelper.navigateWithSectionAdCheck(context, route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
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
            MagicRatingDialog()
        }
    }
}
