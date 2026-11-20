/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import android.view.MotionEvent

/**
 * Interprets a waveform pointer event and reports whether it was consumed.
 *
 * @param action [MotionEvent] action, such as [MotionEvent.ACTION_DOWN].
 * @param x Horizontal position of the pointer, in pixels, relative to the waveform canvas.
 * @param waveformWidthPx Width of the drawn waveform, in pixels. Touches past this are ignored.
 * @param lineWidthPx Width of a waveform line (and of the cursor), in pixels.
 * @param linePaddingPx Padding between waveform lines, in pixels.
 * @param currentSeekProgress In-progress drag progress (`0` to `1`), or `null` if no drag is active.
 * @param onDisallowParentIntercept Called with `true` when a drag starts, and `false` when it ends,
 * so a parent scroller does not steal the gesture.
 * @param onUpdateSeekProgress Called with the current drag progress, or `null` when the drag ends.
 * @param onCommitSeek Called with the final progress when the user lifts their finger after a drag.
 * @return `true` when the event was consumed by the waveform.
 */
internal fun handleWaveformPointerEvent(
    action: Int,
    x: Float,
    waveformWidthPx: Float,
    lineWidthPx: Float = 0f,
    linePaddingPx: Float = 0f,
    currentSeekProgress: Float?,
    onDisallowParentIntercept: (Boolean) -> Unit,
    onUpdateSeekProgress: (Float?) -> Unit,
    onCommitSeek: (Float) -> Unit,
): Boolean {
    return when (action) {
        MotionEvent.ACTION_DOWN -> {
            val progress = seekProgressFromPointerX(x, waveformWidthPx, lineWidthPx, linePaddingPx)
            if (progress != null) {
                onDisallowParentIntercept(true)
                onUpdateSeekProgress(progress)
                true
            } else {
                false
            }
        }
        MotionEvent.ACTION_MOVE -> {
            val progress = seekProgressFromPointerX(x, waveformWidthPx, lineWidthPx, linePaddingPx)
            if (progress != null) {
                onUpdateSeekProgress(progress)
            }
            true
        }
        MotionEvent.ACTION_UP -> {
            onDisallowParentIntercept(false)
            currentSeekProgress?.let(onCommitSeek)
            onUpdateSeekProgress(null)
            true
        }
        MotionEvent.ACTION_CANCEL -> {
            onDisallowParentIntercept(false)
            onUpdateSeekProgress(null)
            true
        }
        else -> false
    }
}

/**
 * Progress for a pointer at [x], so that the cursor is centred under the pointer. This is the inverse of [cursorOffsetPx].
 *
 * @return The progress, from 0 to 1, or `null` if [x] is outside the waveform.
 */
internal fun seekProgressFromPointerX(
    x: Float,
    waveformWidthPx: Float,
    lineWidthPx: Float = 0f,
    linePaddingPx: Float = 0f,
): Float? {
    if (x !in 0F..waveformWidthPx) return null
    val trackWidthPx = cursorTrackWidthPx(waveformWidthPx, lineWidthPx, linePaddingPx)
    if (trackWidthPx <= 0f) return 0f
    return ((x - lineWidthPx / 2) / trackWidthPx).coerceIn(0f, 1f)
}

/**
 * Left position of the cursor, in pixels, so that it always stays over the drawn lines.
 * At `0` the cursor is on the first line, and at `1` its right edge is aligned with the right edge of the last line.
 *
 * @param progress The playback progress, from 0 to 1. Values outside this range are clamped.
 * @param waveformWidthPx Width of the drawn waveform, in pixels, including the padding after the last line.
 * @param lineWidthPx Width of a waveform line (and of the cursor), in pixels.
 * @param linePaddingPx Padding between waveform lines, in pixels.
 */
internal fun cursorOffsetPx(
    progress: Float,
    waveformWidthPx: Float,
    lineWidthPx: Float,
    linePaddingPx: Float,
): Float {
    return progress.coerceIn(0f, 1f) * cursorTrackWidthPx(waveformWidthPx, lineWidthPx, linePaddingPx)
}

/**
 * Distance the cursor can travel: the waveform width minus the padding after the last line and the cursor width.
 */
private fun cursorTrackWidthPx(
    waveformWidthPx: Float,
    lineWidthPx: Float,
    linePaddingPx: Float,
): Float {
    return (waveformWidthPx - linePaddingPx - lineWidthPx).coerceAtLeast(0f)
}
