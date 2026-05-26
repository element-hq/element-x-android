/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NormalizedCropRectTest {
    private val rect = NormalizedCropRect(
        left = 0.1f,
        top = 0.2f,
        right = 0.7f,
        bottom = 0.8f,
    )

    @Test
    fun `applyChange with top handle only updates the top edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Edge.Top,
            deltaX = 0.3f,
            deltaY = 0.1f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = rect.left,
                top = 0.3f,
                right = rect.right,
                bottom = rect.bottom,
            )
        )
    }

    @Test
    fun `applyChange with left handle only updates the left edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Edge.Left,
            deltaX = 0.1f,
            deltaY = 0.3f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = 0.2f,
                top = rect.top,
                right = rect.right,
                bottom = rect.bottom,
            )
        )
    }

    @Test
    fun `applyChange with right handle only updates the right edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Edge.Right,
            deltaX = -0.1f,
            deltaY = 0.3f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = rect.left,
                top = rect.top,
                right = 0.6f,
                bottom = rect.bottom,
            )
        )
    }

    @Test
    fun `applyChange with bottom handle target only updates the bottem edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Edge.Bottom,
            deltaX = -0.1f,
            deltaY = -0.3f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = rect.left,
                top = rect.top,
                right = rect.right,
                bottom = 0.5f,
            )
        )
    }

    @Test
    fun `applyChange with top left handle updates the top and left bottem edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Corner.TopLeft,
            deltaX = 0.1f,
            deltaY = 0.1f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = 0.2f,
                top = 0.3f,
                right = rect.right,
                bottom = rect.bottom,
            )
        )
    }

    @Test
    fun `applyChange with top right handle updates the top and right bottem edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Corner.TopRight,
            deltaX = -0.1f,
            deltaY = 0.1f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = rect.left,
                top = 0.3f,
                right = 0.6f,
                bottom = rect.bottom,
            )
        )
    }

    @Test
    fun `applyChange with bottom left handle updates the bottom and left bottem edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Corner.BottomLeft,
            deltaX = 0.1f,
            deltaY = -0.1f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = 0.2f,
                top = rect.top,
                right = rect.right,
                bottom = 0.7f,
            )
        )
    }

    @Test
    fun `applyChange with bottom right handle updates the bottom and right bottem edge`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Corner.BottomRight,
            deltaX = -0.1f,
            deltaY = -0.1f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = rect.left,
                top = rect.top,
                right = 0.6f,
                bottom = 0.7f,
            )
        )
    }

    @Test
    fun `translate keeps the crop rect inside bounds`() {
        val result = rect.applyChange(
            dragTarget = CropDragTarget.Move,
            deltaX = 0.6f,
            deltaY = 0.6f,
        )
        result.assertIsSimilarTo(
            NormalizedCropRect(
                left = 0.4f,
                top = 0.4f,
                right = 1.0f,
                bottom = 1.0f,
            )
        )
    }
}

internal fun NormalizedCropRect.assertIsSimilarTo(expectedResult: NormalizedCropRect) {
    assertThat(left).isWithin(0.0001f).of(expectedResult.left)
    assertThat(top).isWithin(0.0001f).of(expectedResult.top)
    assertThat(right).isWithin(0.0001f).of(expectedResult.right)
    assertThat(bottom).isWithin(0.0001f).of(expectedResult.bottom)
}
