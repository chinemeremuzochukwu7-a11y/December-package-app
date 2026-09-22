package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.model.WishCategory
import com.example.ui.screens.AiWishGeneratorScreen
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.CreateWishScreen
import com.example.ui.screens.CustomizeCardScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyCardsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WishesScreen
import com.example.ui.viewmodel.HolidayViewModel

@Composable
fun HolidayNavHost(
    navController: NavHostController,
    viewModel: HolidayViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCreateWish = {
                    navController.navigate(Screen.AiWishGenerator.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAiWishGenerator = {
                    navController.navigate(Screen.AiWishGenerator.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCards = {
                    navController.navigate(Screen.Cards.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCustomizeCard = { template ->
                    viewModel.prepareCardForCustomization(template)
                    navController.navigate(Screen.CustomizeCard.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToWishesCategory = { category ->
                    viewModel.selectCategory(category)
                    navController.navigate(Screen.Wishes.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Wishes.route) {
            WishesScreen(viewModel = viewModel)
        }

        composable(Screen.CreateWish.route) {
            AiWishGeneratorScreen(
                viewModel = viewModel,
                onNavigateToCreateCard = {
                    navController.navigate(Screen.CustomizeCard.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AiWishGenerator.route) {
            AiWishGeneratorScreen(
                viewModel = viewModel,
                onNavigateToCreateCard = {
                    navController.navigate(Screen.CustomizeCard.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(
                viewModel = viewModel,
                onNavigateToCustomize = { template ->
                    viewModel.prepareCardForCustomization(template)
                    navController.navigate(Screen.CustomizeCard.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMyCards = {
                    navController.navigate(Screen.MyCards.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.CustomizeCard.route) {
            CustomizeCardScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMyCards = {
                    navController.navigate(Screen.MyCards.route) {
                        popUpTo(Screen.Cards.route)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.MyCards.route) {
            MyCardsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTemplates = {
                    navController.navigate(Screen.Cards.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onExploreWishesClick = {
                    navController.navigate(Screen.Wishes.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
    }
}
