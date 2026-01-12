package com.tscorp.pokus.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens for spacing following 8dp grid system.
 * Provides consistent spacing throughout the app following best practices from Material Design.
 */
object Spacing {
    // Base spacing unit
    val base = 8.dp

    // Extra small spacing
    val xs: Dp = 4.dp

    // Small spacing
    val sm: Dp = 8.dp

    // Medium spacing (default)
    val md: Dp = 16.dp

    // Large spacing
    val lg: Dp = 24.dp

    // Extra large spacing
    val xl: Dp = 32.dp

    // Extra extra large spacing
    val xxl: Dp = 48.dp

    // Extra extra extra large spacing
    val xxxl: Dp = 64.dp
}

/**
 * Design tokens for corner radius following modern design principles.
 */
object CornerRadius {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val full: Dp = 999.dp // For pills/circles
}

/**
 * Design tokens for elevation following Material Design 3.
 */
object Elevation {
    val none: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
}

/**
 * Design tokens for icon sizes.
 */
object IconSize {
    val xs: Dp = 16.dp
    val sm: Dp = 20.dp
    val md: Dp = 24.dp
    val lg: Dp = 32.dp
    val xl: Dp = 48.dp
    val xxl: Dp = 64.dp
}
