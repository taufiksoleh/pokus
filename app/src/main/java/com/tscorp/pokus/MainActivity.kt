package com.tscorp.pokus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tscorp.pokus.ui.theme.PokusTheme
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
                    // TODO: Replace with navigation graph in Phase 6
                    MainContent()
                }
            }
        }
    }
}

/**
 * Main content placeholder.
 * Will be replaced with navigation in Phase 6.
 */
@Composable
private fun MainContent(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Text(
            text = "Pokus - Focus Mode",
            modifier = Modifier.padding(innerPadding),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainContentPreview() {
    PokusTheme {
        MainContent()
    }
}
