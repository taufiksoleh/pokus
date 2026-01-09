package com.tscorp.pokus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tscorp.pokus.ui.navigation.PokusNavGraph
import com.tscorp.pokus.ui.navigation.Routes
import com.tscorp.pokus.ui.theme.PokusTheme
import com.tscorp.pokus.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Pokus app.
 * Handles navigation setup and initial permission checks.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PokusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokusApp()
                }
            }
        }
    }
}

/**
 * Main app composable with navigation setup.
 * Determines the start destination based on permission status.
 */
@Composable
private fun PokusApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Check if all required permissions are granted
    val hasAllPermissions by remember {
        mutableStateOf(PermissionUtils.hasAllRequiredPermissions(context))
    }

    // Determine start destination based on permission status
    val startDestination = if (hasAllPermissions) {
        Routes.Home.route
    } else {
        Routes.Permissions.route
    }

    PokusNavGraph(
        navController = navController,
        startDestination = startDestination
    )
}
