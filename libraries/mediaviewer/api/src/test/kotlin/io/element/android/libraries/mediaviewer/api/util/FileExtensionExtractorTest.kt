/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileExtensionExtractorTest {
    @Test
    fun `test FileExtensionExtractor with validation OK`() {
        val sut = FileExtensionExtractorWithValidation()
        // The result should be txt, but with Robolectric,
        // MimeTypeMap.getSingleton().hasExtension() always returns false
        assertThat(sut.extractFromName("test.txt")).isEqualTo("bin")
    }

    @Test
    fun `test FileExtensionExtractor with validation ERROR`() {
        val sut = FileExtensionExtractorWithValidation()
        assertThat(sut.extractFromName("test.bla")).isEqualTo("bin")
    }

    @Test
    fun `test FileExtensionExtractor no validation`() {
        val sut = FileExtensionExtractorWithoutValidation()
        assertThat(sut.extractFromName("test.png")).isEqualTo("png")
        assertThat(sut.extractFromName("test.bla")).isEqualTo("bla")
    }
}
