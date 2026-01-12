package com.tscorp.pokus.ui.screens.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tscorp.pokus.ui.components.BlockedAppCard
import com.tscorp.pokus.ui.components.ProductivityCard
import com.tscorp.pokus.ui.screens.pomodoro.PomodoroViewModel
import com.tscorp.pokus.util.AppUtils

/**
 * Home screen displaying the focus mode toggle and blocked apps list.
 *
 * @param onNavigateToAppList Callback to navigate to the app list screen
 * @param onNavigateToSettings Callback to navigate to the settings screen
 * @param viewModel The HomeViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAppList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    pomodoroViewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val pomodoroState by pomodoroViewModel.pomodoroState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pokus",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAppList,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add apps to block"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unified Productivity Card (Focus Mode + Pomodoro Timer)
            item {
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

            // Blocked Apps Section Header
            if (blockedApps.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blocked Apps (${blockedApps.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
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

                    BlockedAppCard(
                        appName = blockedApp.appName,
                        packageName = blockedApp.packageName,
                        icon = appIcon,
                        onRemove = { viewModel.unblockApp(blockedApp.packageName) }
                    )
                }
            } else {
                // Empty state
                item {
                    EmptyBlockedAppsState(
                        onAddApps = onNavigateToAppList
                    )
                }
            }
        }
    }
}

/**
 * Empty state displayed when no apps are blocked.
 */
@Composable
private fun EmptyBlockedAppsState(
    onAddApps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No apps blocked yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add apps to block",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Get app icon for a package name.
 */
private fun getAppIcon(context: Context, packageName: String) =
    AppUtils.getAppIcon(context, packageName)
