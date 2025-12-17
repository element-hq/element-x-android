/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.test.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileExtensionExtractorWithoutValidationTest {
    @Test
    fun `extension should always be extracted even is invalid`() {
        val sut = FileExtensionExtractorWithoutValidation()
        assertThat(sut.extractFromName("test.png")).isEqualTo("png")
        assertThat(sut.extractFromName("test.bla")).isEqualTo("bla")
    }
}
