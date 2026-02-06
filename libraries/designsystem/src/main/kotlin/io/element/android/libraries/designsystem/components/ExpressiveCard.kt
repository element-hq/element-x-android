/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

/**
 * Expressive Card with Material 3 motion and dynamic elevation changes.
 * Provides better visual feedback on interaction with lifted appearance when pressed.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    defaultElevation: Dp = 1.dp,
    hoveredElevation: Dp = 4.dp,
    pressedElevation: Dp = 2.dp,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isPressedState = interactionSource.collectIsPressedAsState()
    val isPressed = isPressedState.value
    val targetElevation = when {
        !enabled -> defaultElevation
        isPressed -> pressedElevation
        else -> hoveredElevation
    }
    
    val animatedElevation = animateDpAsState(
        targetValue = targetElevation,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "card_elevation"
    )

    Card(
        modifier = modifier,
        colors = colors,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = animatedElevation.value
        ),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        content = { content() }
    )
}

/**
 * Expressive elevated Card for more prominent elements.
 * Uses stronger elevation and animation for important cards.
 */
@Composable
fun ExpressiveElevatedCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    defaultElevation: Dp = 2.dp,
    hoveredElevation: Dp = 8.dp,
    pressedElevation: Dp = 4.dp,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    ExpressiveCard(
        modifier = modifier,
        colors = colors,
        defaultElevation = defaultElevation,
        hoveredElevation = hoveredElevation,
        pressedElevation = pressedElevation,
        enabled = enabled,
        interactionSource = interactionSource,
        onClick = onClick,
        content = content
    )
}
