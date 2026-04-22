/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import org.junit.Test

class AttachmentImageEditModelsTest {
    @Test
    fun `resize with top handle only updates the top edge`() {
        val rect = NormalizedCropRect(
            left = 0.2f,
            top = 0.2f,
            right = 0.8f,
            bottom = 0.8f,
        )

        val resized = rect.resize(
            dragTarget = CropDragTarget.Top,
            deltaX = 0.3f,
            deltaY = 0.1f,
        )

        assertThat(resized.left).isEqualTo(rect.left)
        assertThat(resized.right).isEqualTo(rect.right)
        assertThat(resized.bottom).isEqualTo(rect.bottom)
        assertThat(resized.top).isEqualTo(0.3f)
    }

    @Test
    fun `translate keeps the crop rect inside bounds`() {
        val rect = NormalizedCropRect(
            left = 0.2f,
            top = 0.2f,
            right = 0.8f,
            bottom = 0.8f,
        )

        val translated = rect.translate(
            deltaX = 0.6f,
            deltaY = 0.6f,
        )

        assertThat(translated.left).isWithin(0.0001f).of(0.4f)
        assertThat(translated.top).isWithin(0.0001f).of(0.4f)
        assertThat(translated.right).isWithin(0.0001f).of(1.0f)
        assertThat(translated.bottom).isWithin(0.0001f).of(1.0f)
    }

    @Test
    fun `rotate clockwise normalizes after a full turn`() {
        var edits = AttachmentImageEdits()

        repeat(4) {
            edits = edits.rotateClockwise()
        }

        assertThat(edits.normalizedRotationQuarterTurns).isEqualTo(0)
        assertThat(edits.rotationDegrees).isEqualTo(0)
        assertThat(edits.hasChanges).isFalse()
    }

    @Test
    fun `exported mime type preserves png`() {
        assertThat(exportedMimeTypeFor(MimeTypes.Png)).isEqualTo(MimeTypes.Png)
    }

    @Test
    fun `exported mime type normalizes non-png images to jpeg`() {
        assertThat(exportedMimeTypeFor("image/heic")).isEqualTo(MimeTypes.Jpeg)
    }
}
