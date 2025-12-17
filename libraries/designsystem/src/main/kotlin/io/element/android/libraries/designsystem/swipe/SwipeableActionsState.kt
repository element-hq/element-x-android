/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Inspired from https://github.com/bmarty/swipe/blob/trunk/swipe/src/main/kotlin/me/saket/swipe/SwipeableActionsState.kt
 */
@Composable
fun rememberSwipeableActionsState(): SwipeableActionsState {
    return remember { SwipeableActionsState() }
}

@Stable
class SwipeableActionsState {
    /**
     * The current position (in pixels) of the content.
     */
    val offset: FloatState get() = offsetState
    private var offsetState = mutableFloatStateOf(0f)

    /**
     * Whether the content is currently animating to reset its offset after it was swiped.
     */
    var isResettingOnRelease: Boolean by mutableStateOf(false)
        private set

    val draggableState = DraggableState { delta ->
        val targetOffset = offsetState.floatValue + delta
        val isAllowed = isResettingOnRelease || targetOffset > 0f

        offsetState.floatValue += if (isAllowed) delta else 0f
    }

    suspend fun resetOffset() {
        draggableState.drag(MutatePriority.PreventUserInput) {
            isResettingOnRelease = true
            try {
                Animatable(offsetState.floatValue).animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300),
                ) {
                    dragBy(value - offsetState.floatValue)
                }
            } finally {
                isResettingOnRelease = false
            }
        }
    }
}
