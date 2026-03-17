/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

private const val DEFAULT_CROP_MARGIN = 0.1f
private const val MIN_CROP_SIZE = 0.1f

@Immutable
data class AttachmentImageEditorState(
    val localMedia: LocalMedia,
    val edits: AttachmentImageEdits = AttachmentImageEdits(),
)

@Immutable
data class AttachmentImageEdits(
    val cropRect: NormalizedCropRect = NormalizedCropRect.default(),
    val rotationQuarterTurns: Int = 0,
) {
    val normalizedRotationQuarterTurns: Int
        get() = ((rotationQuarterTurns % 4) + 4) % 4

    val rotationDegrees: Int
        get() = normalizedRotationQuarterTurns * 90

    val hasChanges: Boolean
        get() = cropRect != NormalizedCropRect.default() || normalizedRotationQuarterTurns != 0

    fun rotateClockwise(): AttachmentImageEdits {
        return copy(rotationQuarterTurns = (normalizedRotationQuarterTurns + 1) % 4)
    }
}

@Immutable
data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(left in 0f..1f)
        require(top in 0f..1f)
        require(right in 0f..1f)
        require(bottom in 0f..1f)
        require(left < right)
        require(top < bottom)
    }

    val width: Float
        get() = right - left

    val height: Float
        get() = bottom - top

    fun translate(deltaX: Float, deltaY: Float): NormalizedCropRect {
        val clampedLeft = (left + deltaX).coerceIn(0f, 1f - width)
        val clampedTop = (top + deltaY).coerceIn(0f, 1f - height)
        return copy(
            left = clampedLeft,
            top = clampedTop,
            right = clampedLeft + width,
            bottom = clampedTop + height,
        )
    }

    fun resize(dragTarget: CropDragTarget, deltaX: Float, deltaY: Float): NormalizedCropRect = when (dragTarget) {
        CropDragTarget.Move -> translate(deltaX, deltaY)
        CropDragTarget.TopLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.Top -> copy(
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.TopRight -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.Right -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.BottomRight -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.Bottom -> copy(
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.BottomLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.Left -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
        )
    }

    companion object {
        fun default() = NormalizedCropRect(
            left = DEFAULT_CROP_MARGIN,
            top = DEFAULT_CROP_MARGIN,
            right = 1f - DEFAULT_CROP_MARGIN,
            bottom = 1f - DEFAULT_CROP_MARGIN,
        )
    }
}

enum class CropDragTarget {
    Move,
    TopLeft,
    Top,
    TopRight,
    Right,
    BottomRight,
    Bottom,
    BottomLeft,
    Left,
}
