package com.tscorp.pokus.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * A card displaying a blocked app with its icon and name.
 *
 * @param appName The name of the blocked app
 * @param packageName The package name of the blocked app
 * @param icon The app icon drawable
 * @param onRemove Callback when the remove button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun BlockedAppCard(
    appName: String,
    packageName: String,
    icon: Drawable?,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            BlockedAppIcon(
                icon = icon,
                contentDescription = appName,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // App Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Blocked indicator or remove button
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove from blocked list",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Blocked",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * A composable that displays a blocked app icon.
 * Caches the bitmap conversion to prevent memory leaks.
 */
@Composable
private fun BlockedAppIcon(
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

    Box(modifier = modifier) {
        if (cachedBitmap != null) {
            Image(
                bitmap = cachedBitmap,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * A compact card for displaying blocked apps in a horizontal list.
 * Caches the bitmap conversion to prevent memory leaks.
 */
@Composable
fun BlockedAppChip(
    appName: String,
    icon: Drawable?,
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

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (cachedBitmap != null) {
                Image(
                    bitmap = cachedBitmap,
                    contentDescription = appName,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = appName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
