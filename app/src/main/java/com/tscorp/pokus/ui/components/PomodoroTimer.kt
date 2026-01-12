package com.tscorp.pokus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.tscorp.pokus.ui.theme.Orange40
import com.tscorp.pokus.ui.theme.Teal40

/**
 * Pomodoro timer component with circular progress indicator and controls.
 *
 * @param pomodoroState Current state of the Pomodoro timer
 * @param onStart Callback when start button is clicked
 * @param onPauseResume Callback when pause/resume button is clicked
 * @param onStop Callback when stop button is clicked
 * @param onSkip Callback when skip button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun PomodoroTimer(
    pomodoroState: PomodoroState,
    onStart: (PomodoroPhase) -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase indicator
            Text(
                text = pomodoroState.phaseDisplayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = getPhaseColor(pomodoroState.phase)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pomodoro count
            if (pomodoroState.completedPomodoros > 0) {
                Text(
                    text = "${pomodoroState.completedPomodoros} pomodoro${if (pomodoroState.completedPomodoros != 1) "s" else ""} completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circular timer
            CircularTimer(
                progress = pomodoroState.progress,
                timeText = pomodoroState.formattedTime,
                phase = pomodoroState.phase,
                isPaused = pomodoroState.isPaused
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            if (pomodoroState.isRunning) {
                // Timer is running - show pause/resume, skip, and stop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop button
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Pause/Resume button (larger)
                    IconButton(
                        onClick = onPauseResume,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(getPhaseColor(pomodoroState.phase).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (pomodoroState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (pomodoroState.isPaused) "Resume" else "Pause",
                            tint = getPhaseColor(pomodoroState.phase),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Skip button
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                // Timer is not running - show start buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Start work session button
                    Button(
                        onClick = { onStart(PomodoroPhase.WORK) },
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

                    // Quick break buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onStart(PomodoroPhase.SHORT_BREAK) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Teal40,
                                contentColor = Color.White
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
                            onClick = { onStart(PomodoroPhase.LONG_BREAK) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Orange40,
                                contentColor = Color.White
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
                }
            }
        }
    }
}

/**
 * Circular timer with progress indicator.
 */
@Composable
private fun CircularTimer(
    progress: Float,
    timeText: String,
    phase: PomodoroPhase,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )

    val phaseColor = getPhaseColor(phase)
    val animatedColor by animateColorAsState(
        targetValue = if (isPaused) phaseColor.copy(alpha = 0.5f) else phaseColor,
        animationSpec = tween(300),
        label = "phaseColor"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(modifier = Modifier.size(240.dp)) {
            val size = size.minDimension
            val strokeWidth = 20.dp.toPx()

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
                text = timeText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = animatedColor,
                textAlign = TextAlign.Center
            )

            if (isPaused) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.labelLarge,
                    color = animatedColor,
                    fontWeight = FontWeight.Bold
                )
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
