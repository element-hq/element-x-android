/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
