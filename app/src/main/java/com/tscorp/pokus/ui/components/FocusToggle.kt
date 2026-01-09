package com.tscorp.pokus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tscorp.pokus.ui.theme.FocusActive
import com.tscorp.pokus.ui.theme.FocusInactive

/**
 * A large toggle button for enabling/disabling focus mode.
 *
 * @param isEnabled Whether focus mode is currently enabled
 * @param onToggle Callback when the toggle is clicked
 * @param focusDuration Formatted string showing focus duration (e.g., "2h 30m")
 * @param blockedAppsCount Number of apps currently blocked
 * @param modifier Modifier for the component
 */
@Composable
fun FocusToggle(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    focusDuration: String,
    blockedAppsCount: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) FocusActive.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isEnabled) FocusActive else FocusInactive,
        animationSpec = tween(300),
        label = "borderColor"
    )

    val iconSize by animateDpAsState(
        targetValue = if (isEnabled) 72.dp else 64.dp,
        animationSpec = tween(300),
        label = "iconSize"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(borderColor, borderColor.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Focus Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) FocusActive.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Focus Mode",
                    modifier = Modifier.size(iconSize),
                    tint = if (isEnabled) FocusActive else FocusInactive
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Text
            Text(
                text = if (isEnabled) "FOCUS MODE" else "FOCUS MODE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) FocusActive else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isEnabled) "ACTIVE" else "INACTIVE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled) FocusActive else FocusInactive
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tap to toggle hint
            Text(
                text = "Tap to ${if (isEnabled) "disable" else "enable"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isEnabled && focusDuration.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Focus Duration
                Text(
                    text = "Focus time: $focusDuration",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = FocusActive
                )
            }

            if (blockedAppsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$blockedAppsCount apps blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
