/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

private const val DEFAULT_CROP_MARGIN = 0f
private const val MIN_CROP_SIZE = 0.1f

@Immutable
data class AttachmentImageEditorState(
    val localMedia: LocalMedia,
    val edits: AttachmentImageEdits,
    // For preview only
    val previewDebug: Boolean,
)

@Immutable
data class AttachmentImageEdits(
    val cropRect: NormalizedCropRect = NormalizedCropRect.default(),
    val rotationQuarterTurns: Int = 0,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
) {
    val normalizedRotationQuarterTurns: Int
        get() = rotationQuarterTurns % 4

    val rotationDegrees: Int
        get() = normalizedRotationQuarterTurns * 90

    val hasChanges: Boolean
        get() = cropRect != NormalizedCropRect.default() ||
            normalizedRotationQuarterTurns != 0 ||
            isFlippedHorizontally ||
            isFlippedVertically

    fun rotateAntiClockwise(): AttachmentImageEdits {
        return copy(
            rotationQuarterTurns = (normalizedRotationQuarterTurns + 3) % 4,
            // Also update the crop rect to keep the same selected area
            cropRect = cropRect.rotateAntiClockwise()
        )
    }

    fun flipHorizontally(): AttachmentImageEdits {
        return copy(
            isFlippedHorizontally = !isFlippedHorizontally,
            // Also update the crop rect to keep the same selected area
            cropRect = cropRect.flipHorizontally(),
        )
    }

    fun flipVertically(): AttachmentImageEdits {
        return copy(
            isFlippedVertically = !isFlippedVertically,
            // Also update the crop rect to keep the same selected area
            cropRect = cropRect.flipVertically(),
        )
    }
}

@Immutable
data class NormalizedCropRect(
    @FloatRange(from = 0.0, to = 1.0) val left: Float,
    @FloatRange(from = 0.0, to = 1.0) val top: Float,
    @FloatRange(from = 0.0, to = 1.0) val right: Float,
    @FloatRange(from = 0.0, to = 1.0) val bottom: Float,
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

    fun applyChange(
        dragTarget: CropDragTarget,
        deltaX: Float,
        deltaY: Float,
    ): NormalizedCropRect = when (dragTarget) {
        is CropDragTarget.Move -> translate(deltaX, deltaY)
        is CropDragTarget.Corner -> dragWithCorner(dragTarget, deltaX, deltaY)
        is CropDragTarget.Edge -> dragWithEdge(dragTarget, deltaX, deltaY)
    }

    private fun translate(deltaX: Float, deltaY: Float): NormalizedCropRect {
        val clampedLeft = (left + deltaX).coerceIn(0f, 1f - width)
        val clampedTop = (top + deltaY).coerceIn(0f, 1f - height)
        return copy(
            left = clampedLeft,
            top = clampedTop,
            right = clampedLeft + width,
            bottom = clampedTop + height,
        )
    }

    private fun dragWithCorner(
        dragTarget: CropDragTarget.Corner,
        deltaX: Float,
        deltaY: Float,
    ) = when (dragTarget) {
        CropDragTarget.Corner.TopLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.Corner.TopRight -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.Corner.BottomRight -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.Corner.BottomLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
    }

    private fun dragWithEdge(
        dragTarget: CropDragTarget.Edge,
        deltaX: Float,
        deltaY: Float,
    ) = when (dragTarget) {
        CropDragTarget.Edge.Top -> copy(
            top = (top + deltaY).coerceIn(0f, bottom - MIN_CROP_SIZE),
        )
        CropDragTarget.Edge.Right -> copy(
            right = (right + deltaX).coerceIn(left + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.Edge.Bottom -> copy(
            bottom = (bottom + deltaY).coerceIn(top + MIN_CROP_SIZE, 1f),
        )
        CropDragTarget.Edge.Left -> copy(
            left = (left + deltaX).coerceIn(0f, right - MIN_CROP_SIZE),
        )
    }

    fun rotateAntiClockwise() = copy(
        left = top,
        top = 1f - right,
        right = bottom,
        bottom = 1f - left,
    )

    fun flipHorizontally() = copy(
        left = 1f - right,
        right = 1f - left,
    )

    fun flipVertically() = copy(
        top = 1f - bottom,
        bottom = 1f - top,
    )

    companion object {
        fun default() = NormalizedCropRect(
            left = DEFAULT_CROP_MARGIN,
            top = DEFAULT_CROP_MARGIN,
            right = 1f - DEFAULT_CROP_MARGIN,
            bottom = 1f - DEFAULT_CROP_MARGIN,
        )
    }
}

sealed interface CropDragTarget {
    data object Move : CropDragTarget

    sealed interface Corner : CropDragTarget {
        data object TopLeft : Corner
        data object TopRight : Corner
        data object BottomRight : Corner
        data object BottomLeft : Corner
    }

    sealed interface Edge : CropDragTarget {
        data object Top : Edge
        data object Right : Edge
        data object Bottom : Edge
        data object Left : Edge
    }
}
