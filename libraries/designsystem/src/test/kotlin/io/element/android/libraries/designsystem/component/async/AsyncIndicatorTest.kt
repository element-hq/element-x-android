/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.component.async

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorItem
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorState
import io.element.android.libraries.designsystem.components.async.hasEntered
import io.element.android.libraries.designsystem.components.async.hasExited
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncIndicatorTest {
    @Test
    fun `initial state`() = runTest {
        val state = AsyncIndicatorState()
        moleculeFlow(RecompositionMode.Immediate) {
            val transitionState = fakeAsyncIndicatorHost(state = state)
            val item = state.currentItem.value
            Snapshot(
                currentItem = item,
                currentAnimationState = TransitionStateSnapshot(transitionState),
            )
        }.test {
            with(awaitItem()) {
                assertThat(currentItem).isNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isFalse()
            }
        }
    }

    @Test
    fun `add item with timeout`() = runTest(StandardTestDispatcher()) {
        val state = AsyncIndicatorState()
        moleculeFlow(RecompositionMode.Immediate) {
            val transitionState = fakeAsyncIndicatorHost(state = state)
            val item = state.currentItem.value
            Snapshot(
                currentItem = item,
                currentAnimationState = TransitionStateSnapshot(transitionState),
            )
        }.test {
            skipItems(1)
            state.enqueue(durationMs = 1000, composable = {})
            // Give it some time to pre-load the events
            advanceTimeBy(1000)
            runCurrent()
            // First, item is invisible but the target state is visible (will start animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is not visible (will start animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Then, item is not visible and the target state is not visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Finally, the current item is removed
            with(awaitItem()) {
                assertThat(currentItem).isNull()
            }
        }
    }

    @Test
    fun `add item without timeout`() = runTest(StandardTestDispatcher()) {
        val state = AsyncIndicatorState()
        moleculeFlow(RecompositionMode.Immediate) {
            val transitionState = fakeAsyncIndicatorHost(state = state)
            val item = state.currentItem.value
            Snapshot(
                currentItem = item,
                currentAnimationState = TransitionStateSnapshot(transitionState),
            )
        }.test {
            skipItems(1)
            state.enqueue(composable = {})
            // First, item is invisible but the target state is visible (will start animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // That's all, the current item will be displayed indefinitely
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `add item without timeout then clear`() = runTest(StandardTestDispatcher()) {
        val state = AsyncIndicatorState()
        moleculeFlow(RecompositionMode.Immediate) {
            val transitionState = fakeAsyncIndicatorHost(state = state)
            val item = state.currentItem.value
            Snapshot(
                currentItem = item,
                currentAnimationState = TransitionStateSnapshot(transitionState),
            )
        }.test {
            skipItems(1)
            state.enqueue(composable = {})
            // First, item is invisible but the target state is visible (will start animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Clear the current item
            state.clear()
            // Animating the exit animation
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Current item is no longer visible
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Finally, the current item is removed
            with(awaitItem()) {
                assertThat(currentItem).isNull()
            }
        }
    }

    @Test
    fun `add item without timeout, then another one`() = runTest(StandardTestDispatcher()) {
        val state = AsyncIndicatorState()
        moleculeFlow(RecompositionMode.Immediate) {
            val transitionState = fakeAsyncIndicatorHost(state = state)
            val item = state.currentItem.value
            Snapshot(
                currentItem = item,
                currentAnimationState = TransitionStateSnapshot(transitionState),
            )
        }.test {
            var firstItem: Any?
            skipItems(1)
            state.enqueue(composable = {})
            state.enqueue(composable = {})
            // First, item is invisible but the target state is visible (will start animating)
            with(awaitItem()) {
                firstItem = currentItem
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Then, item is visible and the target state is not visible (will start animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Then, item is not visible and the target state is not visible (stopped animating)
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isFalse()
            }
            // Then a new item will be not visible and its target animation visible
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(firstItem).isNotEqualTo(currentItem)
                assertThat(currentAnimationState.currentState).isFalse()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // Finally, the second item is visible and not animating
            with(awaitItem()) {
                assertThat(currentItem).isNotNull()
                assertThat(firstItem).isNotEqualTo(currentItem)
                assertThat(currentAnimationState.currentState).isTrue()
                assertThat(currentAnimationState.targetState).isTrue()
            }
            // That's all, the current item will be displayed indefinitely
            ensureAllEventsConsumed()
        }
    }

    @Composable
    private fun fakeAsyncIndicatorHost(state: AsyncIndicatorState): Transition<Boolean>? {
        val coroutineScope = rememberCoroutineScope()
        val transition = state.currentItem.value?.let {
            // If there is an item, update its transition state to simulate an animation
            rememberTransition(state.currentAnimationState, label = "")
        }
        if (state.currentAnimationState.hasEntered() && state.currentItem.value?.durationMs != null) {
            SideEffect {
                coroutineScope.launch {
                    delay(state.currentItem.value!!.durationMs!!)
                    state.nextState()
                }
            }
        } else if (state.currentItem.value != null && state.currentAnimationState.hasExited()) {
            SideEffect {
                state.nextState()
            }
        }
        return transition
    }

    private data class Snapshot(
        val currentItem: AsyncIndicatorItem?,
        val currentAnimationState: TransitionStateSnapshot,
    )

    private data class TransitionStateSnapshot(
        val currentState: Boolean,
        val targetState: Boolean,
    ) {
        constructor(transition: Transition<Boolean>?) : this(
            currentState = transition?.currentState ?: false,
            targetState = transition?.targetState ?: false,
        )
    }
}
