package com.tscorp.pokus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tscorp.pokus.ui.screens.applist.AppListScreen
import com.tscorp.pokus.ui.screens.home.HomeScreen
import com.tscorp.pokus.ui.screens.permissions.PermissionsScreen
import com.tscorp.pokus.ui.screens.settings.SettingsScreen

/**
 * Main navigation graph for the Pokus app.
 *
 * @param navController Navigation controller for managing navigation
 * @param startDestination Initial destination route
 * @param modifier Modifier for the NavHost
 */
@Composable
fun PokusNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.Permissions.route) {
            PermissionsScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToAppList = {
                    navController.navigate(Routes.AppList.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                }
            )
        }

        composable(Routes.AppList.route) {
            AppListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
