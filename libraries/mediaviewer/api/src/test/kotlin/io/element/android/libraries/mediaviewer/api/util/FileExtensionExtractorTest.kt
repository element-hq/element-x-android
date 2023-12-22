/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
