/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.async

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class AsyncIndicatorState {
    private val queue = SnapshotStateList<AsyncIndicatorItem>()
    val currentItem = mutableStateOf<AsyncIndicatorItem?>(null)
    val currentAnimationState = MutableTransitionState(false)

    /**
     * Enqueue a new indicator to be displayed.
     * @param durationMs The duration to display the indicator, if `null` (the default value) it will be displayed indefinitely, until the next indicator is
     * displayed or the current one is manually cleared.
     * @param composable The composable to display.
     */
    fun enqueue(durationMs: Long? = null, composable: @Composable () -> Unit) {
        queue.add(AsyncIndicatorItem(composable, durationMs))
        if (currentItem.value == null || currentItem.value?.durationMs == null) {
            nextState()
        }
    }

    internal fun nextState() {
        if (!currentAnimationState.isIdle) return

        if (currentItem.value != null && currentAnimationState.currentState && currentAnimationState.isIdle) {
            // Is visible and not animating, start the exit animation
            currentAnimationState.targetState = false
        } else if (currentItem.value == null || !currentAnimationState.currentState && currentAnimationState.isIdle) {
            // Not visible or present, start the enter animation for the next item
            val newItem = queue.removeFirstOrNull()
            if (newItem != null) {
                currentItem.value = null
                currentAnimationState.targetState = true
            }
            currentItem.value = newItem
        }
    }

    /**
     * Clear the current indicator using its exit animation.
     */
    fun clear() {
        currentAnimationState.targetState = false
    }
}

/**
 * An item to be displayed in the [AsyncIndicatorHost].
 */
data class AsyncIndicatorItem(
    val composable: @Composable () -> Unit,
    val durationMs: Long? = null,
)

/**
 * Remember an [AsyncIndicatorState] instance.
 */
@Composable
fun rememberAsyncIndicatorState(): AsyncIndicatorState {
    return remember { AsyncIndicatorState() }
}

/**
 * A host for displaying async indicators.
 * @param modifier The modifier to apply.
 * @param state The [AsyncIndicatorState] which values this component will display.
 * @param enterTransition The enter transition to use for the displayed indicators.
 * @param exitTransition The exit transition to use for the hiding indicators.
 */
@Composable
fun AsyncIndicatorHost(
    modifier: Modifier = Modifier,
    state: AsyncIndicatorState = rememberAsyncIndicatorState(),
    enterTransition: EnterTransition = fadeIn(spring(stiffness = 500F)) + slideInVertically(),
    exitTransition: ExitTransition = fadeOut(spring(stiffness = 500F)) + slideOutVertically(),
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (LocalInspectionMode.current) {
            state.currentItem.value?.composable?.invoke()
        } else {
            state.currentItem.value?.let { item ->
                AnimatedVisibility(
                    visibleState = state.currentAnimationState,
                    enter = enterTransition,
                    exit = exitTransition,
                ) {
                    item.composable()
                }

                if (state.currentAnimationState.hasEntered() && item.durationMs != null) {
                    SideEffect {
                        coroutineScope.launch {
                            delay(item.durationMs)
                            state.nextState()
                        }
                    }
                } else if (state.currentAnimationState.hasExited()) {
                    SideEffect {
                        state.nextState()
                    }
                }
            }
        }
    }
}

internal fun MutableTransitionState<Boolean>.hasEntered() = currentState && isIdle
internal fun MutableTransitionState<Boolean>.hasExited() = !currentState && isIdle
