/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecoveryKeyVisualTransformationTest {
    @Test
    fun `RecoveryKeyOffsetMapping computes correct originalToTransformed values`() {
        var sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)

        sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("a")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)
        assertThat(sut.originalToTransformed(1)).isEqualTo(1)

        sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("ab")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)
        assertThat(sut.originalToTransformed(1)).isEqualTo(1)
        assertThat(sut.originalToTransformed(2)).isEqualTo(2)

        sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("abc")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)
        assertThat(sut.originalToTransformed(1)).isEqualTo(1)
        assertThat(sut.originalToTransformed(2)).isEqualTo(2)
        assertThat(sut.originalToTransformed(3)).isEqualTo(3)

        sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("abcd")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)
        assertThat(sut.originalToTransformed(1)).isEqualTo(1)
        assertThat(sut.originalToTransformed(2)).isEqualTo(2)
        assertThat(sut.originalToTransformed(3)).isEqualTo(3)
        assertThat(sut.originalToTransformed(4)).isEqualTo(4)

        sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("abcde")
        assertThat(sut.originalToTransformed(0)).isEqualTo(0)
        assertThat(sut.originalToTransformed(1)).isEqualTo(1)
        assertThat(sut.originalToTransformed(2)).isEqualTo(2)
        assertThat(sut.originalToTransformed(3)).isEqualTo(3)
        assertThat(sut.originalToTransformed(4)).isEqualTo(5)
        assertThat(sut.originalToTransformed(5)).isEqualTo(6)
    }

    @Test
    fun `RecoveryKeyOffsetMapping computes correct transformedToOriginal values`() {
        // text parameter is not used by transformedToOriginal
        val sut = RecoveryKeyVisualTransformation.RecoveryKeyOffsetMapping("")
        assertThat(sut.transformedToOriginal(0)).isEqualTo(0)
        assertThat(sut.transformedToOriginal(1)).isEqualTo(1)
        assertThat(sut.transformedToOriginal(2)).isEqualTo(2)
        assertThat(sut.transformedToOriginal(3)).isEqualTo(3)
        assertThat(sut.transformedToOriginal(4)).isEqualTo(4)
        assertThat(sut.transformedToOriginal(5)).isEqualTo(4)
        assertThat(sut.transformedToOriginal(6)).isEqualTo(5)
        assertThat(sut.transformedToOriginal(7)).isEqualTo(6)
        assertThat(sut.transformedToOriginal(8)).isEqualTo(7)
        assertThat(sut.transformedToOriginal(9)).isEqualTo(8)
        assertThat(sut.transformedToOriginal(10)).isEqualTo(8)
    }
}
