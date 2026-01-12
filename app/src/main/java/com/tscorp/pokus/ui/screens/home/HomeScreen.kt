package com.tscorp.pokus.ui.screens.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tscorp.pokus.ui.components.BlockedAppCard
import com.tscorp.pokus.ui.components.ProductivityCard
import com.tscorp.pokus.ui.screens.pomodoro.PomodoroViewModel
import com.tscorp.pokus.ui.theme.Spacing
import com.tscorp.pokus.util.AppUtils

/**
 * Modern, clean home screen with bottom navigation.
 * Displays productivity card and blocked apps list.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    pomodoroViewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val pomodoroState by pomodoroViewModel.pomodoroState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.lg,
            vertical = Spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // Welcome Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Stay productive, eliminate distractions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Unified Productivity Card (Focus Mode + Pomodoro Timer)
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + scaleOut()
            ) {
                ProductivityCard(
                    focusEnabled = focusState.isEnabled,
                    focusDuration = focusState.formattedDuration,
                    blockedAppsCount = focusState.blockedAppsCount,
                    pomodoroState = pomodoroState,
                    onFocusToggle = { viewModel.toggleFocusMode() },
                    onPomodoroStart = { phase -> pomodoroViewModel.startTimer(phase) },
                    onPomodoroPauseResume = { pomodoroViewModel.togglePauseResume() },
                    onPomodoroStop = { pomodoroViewModel.stopTimer() },
                    onPomodoroSkip = { pomodoroViewModel.skipPhase() }
                )
            }
        }

        // Blocked Apps Section
        if (blockedApps.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Blocked Apps",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "${blockedApps.size} ${if (blockedApps.size == 1) "app" else "apps"} will be blocked during focus mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Blocked Apps List
            items(
                items = blockedApps,
                key = { it.packageName }
            ) { blockedApp ->
                val appIcon = remember(blockedApp.packageName) {
                    getAppIcon(context, blockedApp.packageName)
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                            scaleIn(initialScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut() + scaleOut()
                ) {
                    BlockedAppCard(
                        appName = blockedApp.appName,
                        packageName = blockedApp.packageName,
                        icon = appIcon,
                        onRemove = { viewModel.unblockApp(blockedApp.packageName) }
                    )
                }
            }
        } else {
            // Empty state
            item {
                EmptyBlockedAppsState()
            }
        }
    }
}

/**
 * Modern empty state with better messaging.
 */
@Composable
private fun EmptyBlockedAppsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📱",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "No apps blocked yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Go to Apps tab to select apps you want to block during focus sessions",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Get app icon for a package name.
 */
private fun getAppIcon(context: Context, packageName: String) =
    AppUtils.getAppIcon(context, packageName)
