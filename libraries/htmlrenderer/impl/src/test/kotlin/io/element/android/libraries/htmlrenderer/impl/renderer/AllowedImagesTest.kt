/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl.renderer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AllowedImagesTest {
    @Test
    fun `an image is not allowed until it is explicitly allowed`() {
        val allowedImages = AllowedImages()
        assertThat("https://example.org/cat.png" in allowedImages).isFalse()

        allowedImages.allow("https://example.org/cat.png")
        assertThat("https://example.org/cat.png" in allowedImages).isTrue()
        assertThat("https://example.org/other.png" in allowedImages).isFalse()
    }

    @Test
    fun `initial identifiers are allowed`() {
        val allowedImages = AllowedImages(initial = setOf("mxc://server/allowed"))
        assertThat("mxc://server/allowed" in allowedImages).isTrue()
    }
}
