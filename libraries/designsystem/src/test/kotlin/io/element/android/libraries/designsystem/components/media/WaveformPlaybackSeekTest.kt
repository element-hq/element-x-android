/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import android.view.MotionEvent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WaveformPlaybackSeekTest {
    @Test
    fun `a down event inside the waveform starts a seek`() {
        val result = handleEvent(action = MotionEvent.ACTION_DOWN, x = 40f)
        assertThat(result.consumed).isTrue()
        assertThat(result.disallowParentIntercept).isTrue()
        assertThat(result.seekProgress).isEqualTo(0.4f)
        assertThat(result.committedSeek).isNull()
    }

    @Test
    fun `a down event outside the waveform is ignored`() {
        val result = handleEvent(action = MotionEvent.ACTION_DOWN, x = 150f)
        assertThat(result.consumed).isFalse()
        assertThat(result.seekProgress).isNull()
        assertThat(result.disallowParentIntercept).isNull()
    }

    @Test
    fun `a move event inside the waveform updates the seek progress`() {
        val result = handleEvent(action = MotionEvent.ACTION_MOVE, x = 80f)
        assertThat(result.consumed).isTrue()
        assertThat(result.seekProgress).isEqualTo(0.8f)
        assertThat(result.disallowParentIntercept).isNull()
    }

    @Test
    fun `a move event outside the waveform is still consumed without updating progress`() {
        val result = handleEvent(action = MotionEvent.ACTION_MOVE, x = 150f)
        assertThat(result.consumed).isTrue()
        assertThat(result.seekProgress).isNull()
    }

    @Test
    fun `an up event commits the in-progress seek`() {
        val result = handleEvent(
            action = MotionEvent.ACTION_UP,
            currentSeekProgress = 0.6f,
        )
        assertThat(result.consumed).isTrue()
        assertThat(result.disallowParentIntercept).isFalse()
        assertThat(result.committedSeek).isEqualTo(0.6f)
        assertThat(result.seekProgress).isNull()
        assertThat(result.clearedSeekProgress).isTrue()
    }

    @Test
    fun `an up event without an in-progress seek does not commit`() {
        val result = handleEvent(action = MotionEvent.ACTION_UP, currentSeekProgress = null)
        assertThat(result.consumed).isTrue()
        assertThat(result.committedSeek).isNull()
        assertThat(result.clearedSeekProgress).isTrue()
    }

    @Test
    fun `a cancel event clears the in-progress seek without committing`() {
        val result = handleEvent(
            action = MotionEvent.ACTION_CANCEL,
            currentSeekProgress = 0.6f,
        )
        assertThat(result.consumed).isTrue()
        assertThat(result.disallowParentIntercept).isFalse()
        assertThat(result.committedSeek).isNull()
        assertThat(result.clearedSeekProgress).isTrue()
    }

    @Test
    fun `unknown motion actions are ignored`() {
        val result = handleEvent(action = MotionEvent.ACTION_POINTER_DOWN)
        assertThat(result.consumed).isFalse()
        assertThat(result.seekProgress).isNull()
        assertThat(result.committedSeek).isNull()
    }

    @Test
    fun `seek progress is null when the pointer is outside the waveform`() {
        assertThat(seekProgressFromPointerX(x = -1f, waveformWidthPx = 100f)).isNull()
        assertThat(seekProgressFromPointerX(x = 101f, waveformWidthPx = 100f)).isNull()
        assertThat(seekProgressFromPointerX(x = 0f, waveformWidthPx = 100f)).isEqualTo(0f)
        assertThat(seekProgressFromPointerX(x = 100f, waveformWidthPx = 100f)).isEqualTo(1f)
    }

    @Test
    fun `cursor is on the first line at the start`() {
        assertThat(cursorOffsetPx(progress = 0f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(0f)
    }

    @Test
    fun `cursor right edge is aligned with the last line at the end`() {
        val offset = cursorOffsetPx(progress = 1f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)
        // The last line ends at waveformWidthPx - linePaddingPx.
        assertThat(offset + 2f).isEqualTo(97f)
    }

    @Test
    fun `cursor offset clamps progress outside of 0 to 1`() {
        assertThat(cursorOffsetPx(progress = -0.5f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(0f)
        assertThat(cursorOffsetPx(progress = 1.5f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(95f)
    }

    @Test
    fun `cursor offset is zero when the waveform is narrower than a line`() {
        assertThat(cursorOffsetPx(progress = 0.5f, waveformWidthPx = 1f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(0f)
        assertThat(seekProgressFromPointerX(x = 1f, waveformWidthPx = 1f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(0f)
    }

    @Test
    fun `seeking at the cursor centre gives back the cursor progress`() {
        val offset = cursorOffsetPx(progress = 0.4f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)
        val progress = seekProgressFromPointerX(x = offset + 1f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)
        assertThat(progress).isWithin(0.0001f).of(0.4f)
    }

    @Test
    fun `seeking near the edges of the waveform is clamped`() {
        assertThat(seekProgressFromPointerX(x = 0f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(0f)
        assertThat(seekProgressFromPointerX(x = 100f, waveformWidthPx = 100f, lineWidthPx = 2f, linePaddingPx = 3f)).isEqualTo(1f)
    }

    private fun handleEvent(
        action: Int,
        x: Float = 0f,
        waveformWidthPx: Float = 100f,
        currentSeekProgress: Float? = null,
    ): HandleResult {
        var disallowParentIntercept: Boolean? = null
        var seekProgress: Float? = currentSeekProgress
        var clearedSeekProgress = false
        var committedSeek: Float? = null
        val consumed = handleWaveformPointerEvent(
            action = action,
            x = x,
            waveformWidthPx = waveformWidthPx,
            currentSeekProgress = currentSeekProgress,
            onDisallowParentIntercept = { disallowParentIntercept = it },
            onUpdateSeekProgress = {
                seekProgress = it
                if (it == null) clearedSeekProgress = true
            },
            onCommitSeek = { committedSeek = it },
        )
        return HandleResult(
            consumed = consumed,
            disallowParentIntercept = disallowParentIntercept,
            seekProgress = seekProgress,
            clearedSeekProgress = clearedSeekProgress,
            committedSeek = committedSeek,
        )
    }

    private data class HandleResult(
        val consumed: Boolean,
        val disallowParentIntercept: Boolean?,
        val seekProgress: Float?,
        val clearedSeekProgress: Boolean,
        val committedSeek: Float?,
    )
}
