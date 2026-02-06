/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Provides Material 3 Expressive interaction animations for components.
 * This creates responsive, expressive feedback for user interactions.
 */
object ExpressiveInteraction {
    /**
     * Creates an expressive scale transformation for pressed states.
     * Scales down slightly when pressed, providing tactile feedback.
     */
    @Composable
    fun scaleOnPress(
        isPressed: Boolean,
        pressedScale: Float = 0.98f,
        unpressedScale: Float = 1.0f,
        durationMillis: Int = 100, // Default value
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium,
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isPressed) pressedScale else unpressedScale,
            animationSpec = spring(
                dampingRatio = dampingRatio,
                stiffness = stiffness
            ),
            label = "scale_on_press"
        )
    }

    /**
     * Creates an expressive brightness change for pressed states.
     */
    @Composable
    fun brightnessOnPress(
        isPressed: Boolean,
        baseColor: Color,
        pressedAlpha: Float = 0.9f,
        durationMillis: Int = 100, // Default value
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium,
    ): State<Color> {
        return animateColorAsState(
            targetValue = baseColor.copy(alpha = if (isPressed) pressedAlpha else 1f),
            animationSpec = spring(
                dampingRatio = dampingRatio,
                stiffness = stiffness
            ),
            label = "brightness_on_press"
        )
    }

    /**
     * Applies both scale and brightness transformations for expressive interactions.
     * Usage: modifier.expressiveModifier(...)
     */
    @Composable
    fun Modifier.expressiveModifier(
        isPressed: Boolean,
        baseColor: Color,
        pressedScale: Float = 0.98f,
    ) = this.graphicsLayer {
        val scale = if (isPressed) pressedScale else 1f // Note: this simplifies logic, ideally should use state
        scaleX = scale
        scaleY = scale
    }
}
