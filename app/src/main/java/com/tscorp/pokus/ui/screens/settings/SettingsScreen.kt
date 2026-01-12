package com.tscorp.pokus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Slider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tscorp.pokus.ui.screens.pomodoro.PomodoroViewModel
import com.tscorp.pokus.ui.theme.CornerRadius
import com.tscorp.pokus.ui.theme.IconSize
import com.tscorp.pokus.ui.theme.Spacing
import kotlin.math.roundToInt

/**
 * Modern settings screen with clean layout.
 * Works with bottom navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    pomodoroViewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pomodoroSettings by pomodoroViewModel.pomodoroSettings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Customize your focus experience",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // App Settings Section
        SettingsSection(title = "App Settings") {
                SettingsSwitchItem(
                    icon = Icons.Default.Apps,
                    title = "Show System Apps",
                    description = "Include system apps in the app list",
                    isChecked = uiState.showSystemApps,
                    onCheckedChange = { viewModel.toggleShowSystemApps() }
                )
            }

            // Pomodoro Settings Section
            SettingsSection(title = "Pomodoro Timer") {
                Column {
                    // Work duration slider
                    SettingsSliderItem(
                        icon = Icons.Default.Timer,
                        title = "Work Session",
                        description = "Duration of focus work sessions",
                        value = pomodoroSettings.workDurationMinutes.toFloat(),
                        onValueChange = { pomodoroViewModel.updateWorkDuration(it.roundToInt()) },
                        valueRange = 1f..120f,
                        valueLabel = "${pomodoroSettings.workDurationMinutes} min"
                    )

                    HorizontalDivider()

                    // Short break slider
                    SettingsSliderItem(
                        icon = Icons.Default.Timer,
                        title = "Short Break",
                        description = "Duration of short breaks",
                        value = pomodoroSettings.shortBreakMinutes.toFloat(),
                        onValueChange = { pomodoroViewModel.updateShortBreak(it.roundToInt()) },
                        valueRange = 1f..30f,
                        valueLabel = "${pomodoroSettings.shortBreakMinutes} min"
                    )

                    HorizontalDivider()

                    // Long break slider
                    SettingsSliderItem(
                        icon = Icons.Default.Timer,
                        title = "Long Break",
                        description = "Duration of long breaks",
                        value = pomodoroSettings.longBreakMinutes.toFloat(),
                        onValueChange = { pomodoroViewModel.updateLongBreak(it.roundToInt()) },
                        valueRange = 5f..60f,
                        valueLabel = "${pomodoroSettings.longBreakMinutes} min"
                    )

                    HorizontalDivider()

                    // Pomodoros until long break slider
                    SettingsSliderItem(
                        icon = Icons.Default.Timer,
                        title = "Pomodoros Until Long Break",
                        description = "Number of work sessions before a long break",
                        value = pomodoroSettings.pomodorosUntilLongBreak.toFloat(),
                        onValueChange = { pomodoroViewModel.updatePomodorosUntilLongBreak(it.roundToInt()) },
                        valueRange = 2f..10f,
                        valueLabel = "${pomodoroSettings.pomodorosUntilLongBreak}"
                    )

                    HorizontalDivider()

                    // Auto-start breaks switch
                    SettingsSwitchItem(
                        icon = Icons.Default.Timer,
                        title = "Auto-Start Breaks",
                        description = "Automatically start break timers after work sessions",
                        isChecked = pomodoroSettings.autoStartBreaks,
                        onCheckedChange = { pomodoroViewModel.updateAutoStartBreaks(it) }
                    )

                    HorizontalDivider()

                    // Auto-start pomodoros switch
                    SettingsSwitchItem(
                        icon = Icons.Default.Timer,
                        title = "Auto-Start Work Sessions",
                        description = "Automatically start work sessions after breaks",
                        isChecked = pomodoroSettings.autoStartPomodoros,
                        onCheckedChange = { pomodoroViewModel.updateAutoStartPomodoros(it) }
                    )
                }
            }

            // Data Section
            SettingsSection(title = "Data") {
                SettingsClickableItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Blocked Apps",
                    description = "${uiState.blockedAppsCount} apps blocked",
                    onClick = { viewModel.showClearDataDialog() }
                )
            }

        // About Section
        SettingsSection(title = "About") {
            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "Version",
                value = "1.0.0"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Clear Data Confirmation Dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataDialog() },
            title = { Text("Clear All Data") },
            text = {
                Text("This will remove all blocked apps and disable focus mode. Are you sure?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAllData() }
                ) {
                    Text(
                        "Clear",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Settings section with a title.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            shape = RoundedCornerShape(CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * A settings item with a switch toggle.
 */
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IconSize.md)
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * A clickable settings item.
 */
@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(IconSize.md)
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A settings info item (non-clickable).
 */
@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IconSize.md)
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A settings item with a slider.
 */
@Composable
private fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(IconSize.md)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            Text(
                text = valueLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
