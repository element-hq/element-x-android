/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AttachmentImageEditsTest {
    @Test
    fun `rotate normalizes after a full turn`() {
        var edits = AttachmentImageEdits()
        repeat(4) {
            edits = edits.rotateAntiClockwise()
        }
        assertThat(edits.normalizedRotationQuarterTurns).isEqualTo(0)
        assertThat(edits.rotationDegrees).isEqualTo(0)
        assertThat(edits.hasChanges).isFalse()
    }

    @Test
    fun `rotate updates rotation and crop`() {
        val sut = AttachmentImageEdits(
            cropRect = NormalizedCropRect(
                left = 0.2f,
                top = 0.3f,
                right = 0.8f,
                bottom = 0.9f,
            ),
            rotationQuarterTurns = 0,
        )
        val result = sut.rotateAntiClockwise()
        assertThat(result.normalizedRotationQuarterTurns).isEqualTo(3)
        assertThat(result.rotationDegrees).isEqualTo(270)
        assertThat(result.cropRect.left).isWithin(0.0001f).of(0.3f)
        assertThat(result.cropRect.top).isWithin(0.0001f).of(0.2f)
        assertThat(result.cropRect.right).isWithin(0.0001f).of(0.9f)
        assertThat(result.cropRect.bottom).isWithin(0.0001f).of(0.8f)
        assertThat(result.hasChanges).isTrue()
    }

    @Test
    fun `flip horizontally updates crop and change tracking`() {
        val sut = AttachmentImageEdits(
            cropRect = NormalizedCropRect(
                left = 0.1f,
                top = 0.3f,
                right = 0.6f,
                bottom = 0.9f,
            )
        )
        val result = sut.flipHorizontally()
        assertThat(result.isFlippedHorizontally).isTrue()
        assertThat(result.cropRect.left).isWithin(0.0001f).of(0.4f)
        assertThat(result.cropRect.right).isWithin(0.0001f).of(0.9f)
        assertThat(result.cropRect.top).isWithin(0.0001f).of(0.3f)
        assertThat(result.cropRect.bottom).isWithin(0.0001f).of(0.9f)
        assertThat(result.hasChanges).isTrue()
    }

    @Test
    fun `flip vertical twice resets to default state`() {
        val edits = AttachmentImageEdits().flipVertically().flipVertically()
        assertThat(edits.isFlippedVertically).isFalse()
        assertThat(edits.hasChanges).isFalse()
    }

    @Test
    fun `flip horizontally twice resets to default state`() {
        val edits = AttachmentImageEdits().flipHorizontally().flipHorizontally()
        assertThat(edits.isFlippedVertically).isFalse()
        assertThat(edits.hasChanges).isFalse()
    }
}
