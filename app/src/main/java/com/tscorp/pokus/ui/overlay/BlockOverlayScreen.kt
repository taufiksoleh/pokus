package com.tscorp.pokus.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tscorp.pokus.ui.theme.PokusTheme
import com.tscorp.pokus.ui.theme.Purple40

/**
 * Fullscreen composable that displays the block overlay.
 * Shows a motivational message when a blocked app is accessed.
 *
 * @param blockedAppName The name of the blocked app
 * @param onGoBackClick Callback when user clicks "Go Back Home"
 * @param modifier Optional modifier for the composable
 */
@Composable
fun BlockOverlayScreen(
    blockedAppName: String,
    onGoBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Block Icon
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = "Blocked",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Title
            Text(
                text = "You need to focus",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Blocked App Name
            Text(
                text = "$blockedAppName is blocked",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Motivational Message
            Text(
                text = "Stay focused on what matters most.\nYou can do this!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Go Back Button
            Button(
                onClick = onGoBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Go Back Home",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockOverlayScreenPreview() {
    PokusTheme(darkTheme = true) {
        BlockOverlayScreen(
            blockedAppName = "Instagram",
            onGoBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockOverlayScreenLightPreview() {
    PokusTheme(darkTheme = false) {
        BlockOverlayScreen(
            blockedAppName = "TikTok",
            onGoBackClick = {}
        )
    }
}
