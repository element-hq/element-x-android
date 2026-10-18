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
    currentSeekProgress: Float?,
    onDisallowParentIntercept: (Boolean) -> Unit,
    onUpdateSeekProgress: (Float?) -> Unit,
    onCommitSeek: (Float) -> Unit,
): Boolean {
    return when (action) {
        MotionEvent.ACTION_DOWN -> {
            val progress = seekProgressFromPointerX(x, waveformWidthPx)
            if (progress != null) {
                onDisallowParentIntercept(true)
                onUpdateSeekProgress(progress)
                true
            } else {
                false
            }
        }
        MotionEvent.ACTION_MOVE -> {
            val progress = seekProgressFromPointerX(x, waveformWidthPx)
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

internal fun seekProgressFromPointerX(x: Float, waveformWidthPx: Float): Float? {
    if (x in 0F..waveformWidthPx) {
        return x / waveformWidthPx
    }
    return null
}
