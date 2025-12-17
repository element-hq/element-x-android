/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileExtensionExtractorWithValidationTest {
    @Test
    fun `test FileExtensionExtractor with validation OK`() {
        val sut = FileExtensionExtractorWithValidation()
        assertThat(sut.extractFromName("test.txt")).isEqualTo("txt")
    }

    @Test
    fun `test FileExtensionExtractor with validation ERROR`() {
        val sut = FileExtensionExtractorWithValidation()
        assertThat(sut.extractFromName("test.bla")).isEqualTo("bin")
    }
}
