package com.tscorp.pokus.ui.navigation

/**
 * Defines all navigation routes in the app.
 */
sealed class Routes(val route: String) {

    /**
     * Permissions screen - shown when required permissions are not granted.
     */
    data object Permissions : Routes("permissions")

    /**
     * Home screen - main dashboard with focus mode toggle.
     */
    data object Home : Routes("home")

    /**
     * App list screen - select apps to block.
     */
    data object AppList : Routes("app_list")

    /**
     * Settings screen - app configuration.
     */
    data object Settings : Routes("settings")
}
