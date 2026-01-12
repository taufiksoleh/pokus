package com.tscorp.pokus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tscorp.pokus.domain.model.PomodoroPhase
import com.tscorp.pokus.domain.model.PomodoroState
import com.tscorp.pokus.ui.theme.FocusActive
import com.tscorp.pokus.ui.theme.FocusInactive
import com.tscorp.pokus.ui.theme.Orange40
import com.tscorp.pokus.ui.theme.Teal40

/**
 * Unified productivity card that integrates Focus Mode and Pomodoro timer.
 * Provides a cohesive experience where starting Pomodoro automatically enables Focus Mode.
 *
 * @param focusEnabled Whether focus mode is currently enabled
 * @param focusDuration Formatted string showing focus duration
 * @param blockedAppsCount Number of apps currently blocked
 * @param pomodoroState Current state of the Pomodoro timer
 * @param onFocusToggle Callback when focus mode toggle is clicked
 * @param onPomodoroStart Callback when start button is clicked
 * @param onPomodoroPauseResume Callback when pause/resume button is clicked
 * @param onPomodoroStop Callback when stop button is clicked
 * @param onPomodoroSkip Callback when skip button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun ProductivityCard(
    focusEnabled: Boolean,
    focusDuration: String,
    blockedAppsCount: Int,
    pomodoroState: PomodoroState,
    onFocusToggle: () -> Unit,
    onPomodoroStart: (PomodoroPhase) -> Unit,
    onPomodoroPauseResume: () -> Unit,
    onPomodoroStop: () -> Unit,
    onPomodoroSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = focusEnabled || pomodoroState.isRunning

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) FocusActive.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (pomodoroState.isRunning) {
            getPhaseColor(pomodoroState.phase)
        } else if (focusEnabled) {
            FocusActive
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header
            FocusStatusHeader(
                focusEnabled = focusEnabled,
                pomodoroState = pomodoroState,
                focusDuration = focusDuration,
                blockedAppsCount = blockedAppsCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Content Area
            if (pomodoroState.isRunning) {
                // Show Pomodoro Timer
                PomodoroTimerDisplay(
                    pomodoroState = pomodoroState,
                    onPauseResume = onPomodoroPauseResume,
                    onStop = onPomodoroStop,
                    onSkip = onPomodoroSkip
                )
            } else {
                // Show Start Options
                PomodoroStartControls(
                    focusEnabled = focusEnabled,
                    onFocusToggle = onFocusToggle,
                    onPomodoroStart = onPomodoroStart
                )
            }
        }
    }
}

/**
 * Header showing current focus and pomodoro status.
 */
@Composable
private fun FocusStatusHeader(
    focusEnabled: Boolean,
    pomodoroState: PomodoroState,
    focusDuration: String,
    blockedAppsCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Icon
        val iconColor = when {
            pomodoroState.isRunning -> getPhaseColor(pomodoroState.phase)
            focusEnabled -> FocusActive
            else -> FocusInactive
        }

        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Focus Status",
            modifier = Modifier.size(48.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = when {
                pomodoroState.isRunning -> pomodoroState.phaseDisplayName
                focusEnabled -> "Focus Mode Active"
                else -> "Productivity Hub"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = iconColor
        )

        // Subtitle
        AnimatedVisibility(
            visible = pomodoroState.completedPomodoros > 0 || (focusEnabled && !pomodoroState.isRunning)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        pomodoroState.completedPomodoros > 0 ->
                            "${pomodoroState.completedPomodoros} pomodoro${if (pomodoroState.completedPomodoros != 1) "s" else ""} completed"
                        focusEnabled && focusDuration.isNotEmpty() ->
                            "Active for $focusDuration"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Blocked apps count
        if (blockedAppsCount > 0 && !pomodoroState.isRunning) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$blockedAppsCount apps blocked",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Circular timer display with controls.
 */
@Composable
private fun PomodoroTimerDisplay(
    pomodoroState: PomodoroState,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = pomodoroState.progress,
        animationSpec = tween(500),
        label = "progress"
    )

    val phaseColor = getPhaseColor(pomodoroState.phase)
    val animatedColor by animateColorAsState(
        targetValue = if (pomodoroState.isPaused) phaseColor.copy(alpha = 0.5f) else phaseColor,
        animationSpec = tween(300),
        label = "phaseColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Timer
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val size = size.minDimension
                val strokeWidth = 16.dp.toPx()

                // Background arc
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size, size),
                    topLeft = Offset(
                        (this.size.width - size) / 2,
                        (this.size.height - size) / 2
                    )
                )

                // Progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            animatedColor,
                            animatedColor.copy(alpha = 0.6f),
                            animatedColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size, size),
                    topLeft = Offset(
                        (this.size.width - size) / 2,
                        (this.size.height - size) / 2
                    )
                )
            }

            // Time text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pomodoroState.formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = animatedColor,
                    textAlign = TextAlign.Center
                )

                if (pomodoroState.isPaused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.labelMedium,
                        color = animatedColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Controls
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop button
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pause/Resume button (larger)
            IconButton(
                onClick = onPauseResume,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(getPhaseColor(pomodoroState.phase).copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (pomodoroState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (pomodoroState.isPaused) "Resume" else "Pause",
                    tint = getPhaseColor(pomodoroState.phase),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Skip button
            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Start controls with focus toggle and pomodoro start buttons.
 */
@Composable
private fun PomodoroStartControls(
    focusEnabled: Boolean,
    onFocusToggle: () -> Unit,
    onPomodoroStart: (PomodoroPhase) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main action button - Start Pomodoro
        Button(
            onClick = { onPomodoroStart(PomodoroPhase.WORK) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FocusActive,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Focus Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary buttons - Break options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onPomodoroStart(PomodoroPhase.SHORT_BREAK) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal40.copy(alpha = 0.3f),
                    contentColor = Teal40
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Short Break",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onPomodoroStart(PomodoroPhase.LONG_BREAK) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange40.copy(alpha = 0.3f),
                    contentColor = Orange40
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Long Break",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Focus mode only toggle (when no pomodoro)
        AnimatedVisibility(
            visible = !focusEnabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onFocusToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Focus Mode Only",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Disable focus mode button (when enabled without pomodoro)
        AnimatedVisibility(
            visible = focusEnabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onFocusToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Disable Focus Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Gets the color associated with a Pomodoro phase.
 */
@Composable
private fun getPhaseColor(phase: PomodoroPhase): Color {
    return when (phase) {
        PomodoroPhase.WORK -> FocusActive
        PomodoroPhase.SHORT_BREAK -> Teal40
        PomodoroPhase.LONG_BREAK -> Orange40
        PomodoroPhase.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
