/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.mimetype

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes.ensureDefaultSubtype
import org.junit.Test

class MimeTypesTest {
    @Test
    fun `ensureDefaultSubtype keeps a mime type that already has a subtype`() {
        assertThat(MimeTypes.Png.ensureDefaultSubtype()).isEqualTo(MimeTypes.Png)
        assertThat(MimeTypes.Mp4.ensureDefaultSubtype()).isEqualTo(MimeTypes.Mp4)
        assertThat(MimeTypes.Pdf.ensureDefaultSubtype()).isEqualTo(MimeTypes.Pdf)
    }

    @Test
    fun `ensureDefaultSubtype replaces a wildcard subtype with a concrete one`() {
        assertThat(MimeTypes.Images.ensureDefaultSubtype()).isEqualTo(MimeTypes.Jpeg)
        assertThat(MimeTypes.Videos.ensureDefaultSubtype()).isEqualTo(MimeTypes.Mp4)
        assertThat(MimeTypes.Audio.ensureDefaultSubtype()).isEqualTo(MimeTypes.Mp3)
    }

    @Test
    fun `ensureDefaultSubtype falls back to octet stream`() {
        assertThat(MimeTypes.Any.ensureDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
        assertThat("text/*".ensureDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
        assertThat(null.ensureDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
    }
}
