/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.runtime.MonotonicFrameClock
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

/**
 * Tests for the workaround of https://github.com/element-hq/element-x-android/issues/8102.
 */
class ModalBottomSheetStuckStateTest {
    private enum class Anchor { Top, Bottom }

    private fun anchors(bottom: Float) = DraggableAnchors {
        Anchor.Top at 0f
        Anchor.Bottom at bottom
    }

    /**
     * Pins the Compose foundation behaviour which causes the issue. If this test starts failing after a Compose upgrade,
     * the bug may have been fixed upstream, and `UnstuckSheetStateEffect` may not be needed anymore.
     */
    @Test
    fun `anchors updated while an untargeted drag holds the lock leave the animation running flag stuck`() = runTest {
        val state = AnchoredDraggableState(initialValue = Anchor.Top, anchors = anchors(bottom = 1000f))
        val dragStarted = CompletableDeferred<Unit>()
        val releaseDrag = CompletableDeferred<Unit>()
        val dragJob = launch {
            // Like the nested scroll fling of the bottom sheet
            state.anchoredDrag {
                dragStarted.complete(Unit)
                releaseDrag.await()
            }
        }
        dragStarted.await()
        // Like the sheet height changing while it is flung to the top
        state.updateAnchors(anchors(bottom = 1100f))
        releaseDrag.complete(Unit)
        dragJob.join()
        assertThat(state.isAnimationRunning).isTrue()
        // A targeted drag clears the stale state
        state.snapTo(state.currentValue)
        assertThat(state.isAnimationRunning).isFalse()
    }

    @Test
    fun `anchors updated without any drag do not leave the animation running flag stuck`() {
        val state = AnchoredDraggableState(initialValue = Anchor.Top, anchors = anchors(bottom = 1000f))
        state.updateAnchors(anchors(bottom = 1100f))
        assertThat(state.isAnimationRunning).isFalse()
    }

    @Test
    fun `awaitStuckAtRest returns false when no animation is running`() = runTest {
        val clock = FakeFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(isAnimationRunning = { false }, offset = { 0f })
        }
        assertThat(result).isFalse()
        assertThat(clock.frameCount).isEqualTo(0)
    }

    @Test
    fun `awaitStuckAtRest returns true when the animation is running but the offset does not change`() = runTest {
        val clock = FakeFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(isAnimationRunning = { true }, offset = { 0f }, stableFrameCount = 3)
        }
        assertThat(result).isTrue()
        assertThat(clock.frameCount).isEqualTo(3)
    }

    @Test
    fun `awaitStuckAtRest waits for the offset to stop changing`() = runTest {
        val clock = FakeFrameClock()
        // The offset changes during the 5 first frames, then stops changing
        val result = withContext(clock) {
            awaitStuckAtRest(
                isAnimationRunning = { true },
                offset = { clock.frameCount.coerceAtMost(5) * 100f },
                stableFrameCount = 3,
            )
        }
        assertThat(result).isTrue()
        assertThat(clock.frameCount).isEqualTo(8)
    }

    @Test
    fun `awaitStuckAtRest returns false when a moving animation ends`() = runTest {
        val clock = FakeFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(
                isAnimationRunning = { clock.frameCount < 10 },
                offset = { clock.frameCount * 100f },
                stableFrameCount = 3,
            )
        }
        assertThat(result).isFalse()
        assertThat(clock.frameCount).isEqualTo(10)
    }

    @Test
    fun `awaitStuckAtRest does not consider an unknown offset as stable`() = runTest {
        val clock = FakeFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(
                isAnimationRunning = { clock.frameCount < 10 },
                offset = { null },
                stableFrameCount = 3,
            )
        }
        assertThat(result).isFalse()
        assertThat(clock.frameCount).isEqualTo(10)
    }

    private class FakeFrameClock : MonotonicFrameClock {
        var frameCount = 0
            private set

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            frameCount++
            return onFrame(frameCount * 16_000_000L)
        }
    }
}
