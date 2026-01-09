package com.tscorp.pokus.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.tscorp.pokus.domain.model.InstalledApp

/**
 * A composable that displays an installed app item with its icon, name, and block status.
 *
 * @param app The installed app to display
 * @param onToggleBlock Callback when the block status is toggled
 * @param modifier Modifier for the component
 */
@Composable
fun AppItem(
    app: InstalledApp,
    onToggleBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleBlock),
        shape = RoundedCornerShape(12.dp),
        color = if (app.isBlocked) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            AppIcon(
                icon = app.icon,
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // App Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Block Checkbox
            Checkbox(
                checked = app.isBlocked,
                onCheckedChange = { onToggleBlock() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * A composable that displays an app icon.
 * Caches the bitmap conversion to prevent memory leaks and improve performance.
 *
 * @param icon The drawable icon to display
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier for the component
 */
@Composable
fun AppIcon(
    icon: Drawable?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    // Cache the bitmap conversion to prevent creating new bitmaps on each recomposition
    val cachedBitmap: ImageBitmap? = remember(icon) {
        icon?.let {
            try {
                it.toBitmap().asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    if (cachedBitmap != null) {
        Image(
            bitmap = cachedBitmap,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
