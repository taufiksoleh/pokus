package com.tscorp.pokus.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tscorp.pokus.ui.components.BottomNavigationBar
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
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Show bottom nav only for main screens (not permissions)
    val showBottomNav = currentRoute != Routes.Permissions.route

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the home destination to avoid building up a large stack
                            popUpTo(Routes.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                HomeScreen()
            }

            composable(Routes.AppList.route) {
                AppListScreen()
            }

            composable(Routes.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
