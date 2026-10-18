/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.component.async

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionIndicatorEffect
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncActionIndicatorTest {
    private val showDelay = 300.milliseconds
    private val minDisplay = 500.milliseconds

    @Test
    fun `loader is not shown before the show delay elapses`() = runTest {
        val state = AsyncIndicatorState()
        val action = mutableStateOf<AsyncAction<*>>(AsyncAction.Uninitialized)
        indicatorFlow(action, state).test {
            action.value = AsyncAction.Loading
            advanceTimeBy(100.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isFalse()
            // Once the show delay has fully elapsed, the loader is shown.
            advanceTimeBy(300.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loader is never shown when the operation completes before the show delay`() = runTest {
        val state = AsyncIndicatorState()
        val action = mutableStateOf<AsyncAction<*>>(AsyncAction.Uninitialized)
        indicatorFlow(action, state).test {
            action.value = AsyncAction.Loading
            advanceTimeBy(100.milliseconds)
            runCurrent()
            action.value = AsyncAction.Success(Unit)
            advanceTimeBy(1000.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure is shown immediately, without waiting for the show delay`() = runTest {
        val state = AsyncIndicatorState()
        val action = mutableStateOf<AsyncAction<*>>(AsyncAction.Uninitialized)
        indicatorFlow(action, state).test {
            action.value = AsyncAction.Failure(Exception("failed"))
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loader stays visible for the minimum duration once shown`() = runTest {
        val state = AsyncIndicatorState()
        val action = mutableStateOf<AsyncAction<*>>(AsyncAction.Uninitialized)
        val timeSource = TestTimeSource()
        indicatorFlow(action, state, timeSource).test {
            // Show the loader.
            action.value = AsyncAction.Loading
            advanceTimeBy(400.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isTrue()
            // 200ms of display has already elapsed when the operation succeeds.
            timeSource += 200.milliseconds
            action.value = AsyncAction.Success(Unit)
            runCurrent()
            // Still within the minimum display duration (200ms shown, 300ms remaining) -> stays visible.
            advanceTimeBy(200.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isTrue()
            // Past the minimum display duration -> cleared.
            advanceTimeBy(200.milliseconds)
            runCurrent()
            assertThat(state.currentAnimationState.targetState).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun indicatorFlow(
        action: State<AsyncAction<*>>,
        state: AsyncIndicatorState,
        timeSource: TimeSource = TimeSource.Monotonic,
    ): Flow<Boolean> = moleculeFlow(RecompositionMode.Immediate) {
        AsyncActionIndicatorEffect(
            asyncAction = action.value,
            state = state,
            showDelay = showDelay,
            minDisplayDuration = minDisplay,
            failureDuration = minDisplay,
            loading = {},
            failure = {},
            timeSource = timeSource,
        )
        state.currentAnimationState.targetState
    }
}
