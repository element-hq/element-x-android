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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.MonotonicFrameClock
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

/**
 * Tests for the workaround of https://github.com/element-hq/element-x-android/issues/8102.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        val clock = FakeMonotonicFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(isAnimationRunning = { false }, offset = { 0f })
        }
        assertThat(result).isFalse()
        assertThat(clock.frameCount).isEqualTo(0)
    }

    @Test
    fun `awaitStuckAtRest returns true when the animation is running but the offset does not change`() = runTest {
        val clock = FakeMonotonicFrameClock()
        val result = withContext(clock) {
            awaitStuckAtRest(isAnimationRunning = { true }, offset = { 0f }, stableFrameCount = 3)
        }
        assertThat(result).isTrue()
        assertThat(clock.frameCount).isEqualTo(3)
    }

    @Test
    fun `awaitStuckAtRest waits for the offset to stop changing`() = runTest {
        val clock = FakeMonotonicFrameClock()
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
        val clock = FakeMonotonicFrameClock()
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
        val clock = FakeMonotonicFrameClock()
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

    @Test
    fun `resetWhenStuck resets a stuck sheet to its current value`() = runTest {
        val sheet = StuckSheet(currentValue = SheetValue.Expanded, targetValue = SheetValue.Expanded)
        withContext(FakeMonotonicFrameClock()) { sheet.resetWhenStuck() }
        assertThat(sheet.animateToCalls).containsExactly(SheetValue.Expanded)
        assertThat(sheet.isAnimationRunning).isFalse()
    }

    @Test
    fun `resetWhenStuck resets a stuck sheet whose stale target differs from its current value`() = runTest {
        val sheet = StuckSheet(currentValue = SheetValue.PartiallyExpanded, targetValue = SheetValue.Expanded)
        withContext(FakeMonotonicFrameClock()) { sheet.resetWhenStuck() }
        assertThat(sheet.animateToCalls).containsExactly(SheetValue.PartiallyExpanded)
    }

    @Test
    fun `resetWhenStuck does nothing when the sheet is hidden`() = runTest {
        val sheet = StuckSheet(currentValue = SheetValue.Hidden, targetValue = SheetValue.Hidden)
        withContext(FakeMonotonicFrameClock()) { sheet.resetWhenStuck() }
        assertThat(sheet.animateToCalls).isEmpty()
    }

    @Test
    fun `resetWhenStuck never interrupts a hide animation`() = runTest {
        val clock = FakeMonotonicFrameClock()
        // The hide animation does not move for a while, then ends
        val sheet = StuckSheet(
            currentValue = SheetValue.Expanded,
            targetValue = SheetValue.Hidden,
            isAnimationRunning = { clock.frameCount < 20 },
        )
        withContext(clock) { sheet.resetWhenStuck() }
        assertThat(sheet.animateToCalls).isEmpty()
    }

    @Test
    fun `resetWhenStuck tries again when the reset is interrupted by a user drag`() = runTest {
        val sheet = StuckSheet(
            currentValue = SheetValue.Expanded,
            targetValue = SheetValue.Expanded,
            interruptedResetCount = 2,
        )
        withContext(FakeMonotonicFrameClock()) { sheet.resetWhenStuck() }
        assertThat(sheet.animateToCalls).containsExactly(SheetValue.Expanded, SheetValue.Expanded, SheetValue.Expanded)
        assertThat(sheet.isAnimationRunning).isFalse()
    }

    @Test
    fun `resetWhenStuck gives up when resetting does not clear the stuck state`() = runTest {
        val sheet = StuckSheet(
            currentValue = SheetValue.Expanded,
            targetValue = SheetValue.Expanded,
            resetClearsStuckState = false,
        )
        withContext(FakeMonotonicFrameClock()) { sheet.resetWhenStuck(maxResets = 3) }
        assertThat(sheet.animateToCalls).hasSize(3)
        assertThat(sheet.isAnimationRunning).isTrue()
    }

    /**
     * A sheet resting at offset 0, with [isAnimationRunning] reported as true until it is reset.
     */
    private class StuckSheet(
        val currentValue: SheetValue,
        val targetValue: SheetValue,
        private var interruptedResetCount: Int = 0,
        private val resetClearsStuckState: Boolean = true,
        isAnimationRunning: (() -> Boolean)? = null,
    ) {
        private var isReset = false
        private val isAnimationRunningProvider = isAnimationRunning ?: { !isReset }
        val isAnimationRunning: Boolean get() = isAnimationRunningProvider()
        val animateToCalls = mutableListOf<SheetValue>()

        suspend fun resetWhenStuck(maxResets: Int = 3) = resetWhenStuck(
            isAnimationRunning = { isAnimationRunning },
            offset = { 0f },
            currentValue = { currentValue },
            targetValue = { targetValue },
            animateTo = { value ->
                animateToCalls += value
                if (interruptedResetCount > 0) {
                    interruptedResetCount--
                    throw CancellationException("Interrupted by a user drag")
                }
                if (resetClearsStuckState) isReset = true
            },
            stableFrameCount = 3,
            maxResets = maxResets,
        )
    }

    private class FakeMonotonicFrameClock : MonotonicFrameClock {
        var frameCount = 0
            private set

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            frameCount++
            return onFrame(frameCount * 16_000_000L)
        }
    }
}
