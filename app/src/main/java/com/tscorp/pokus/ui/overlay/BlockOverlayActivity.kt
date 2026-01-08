package com.tscorp.pokus.ui.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tscorp.pokus.ui.theme.PokusTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that displays a fullscreen overlay when a blocked app is opened.
 * This activity appears on top of the blocked app with a "You need to focus" message.
 *
 * Key features:
 * - Appears over blocked apps
 * - Shows motivational message
 * - Provides option to go back home
 * - Cannot be bypassed easily
 */
@AndroidEntryPoint
class BlockOverlayActivity : ComponentActivity() {

    private var blockedAppName: String = ""
    private var blockedPackageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        blockedAppName = intent.getStringExtra(EXTRA_APP_NAME) ?: "App"
        blockedPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        setContent {
            PokusTheme(darkTheme = true) {
                BlockOverlayScreen(
                    blockedAppName = blockedAppName,
                    onGoBackClick = { navigateToHome() }
                )
            }
        }
    }

    /**
     * Navigates user back to the home screen.
     */
    private fun navigateToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    /**
     * Prevent back press from dismissing the overlay easily.
     * User must tap "Go Back Home" button.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intentionally prevent default back behavior
        // User must use the "Go Back Home" button
        navigateToHome()
    }

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"

        /**
         * Creates an intent to launch the block overlay.
         *
         * @param context The context to create the intent from
         * @param appName The name of the blocked app to display
         * @param packageName The package name of the blocked app
         */
        fun createIntent(
            context: Context,
            appName: String,
            packageName: String
        ): Intent {
            return Intent(context, BlockOverlayActivity::class.java).apply {
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}
