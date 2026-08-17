/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.mimetype

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes.withDefaultSubtype
import org.junit.Test

class MimeTypesTest {
    @Test
    fun `withDefaultSubtype keeps a mime type that already has a subtype`() {
        assertThat(MimeTypes.Png.withDefaultSubtype()).isEqualTo(MimeTypes.Png)
        assertThat(MimeTypes.Mp4.withDefaultSubtype()).isEqualTo(MimeTypes.Mp4)
        assertThat(MimeTypes.Pdf.withDefaultSubtype()).isEqualTo(MimeTypes.Pdf)
    }

    @Test
    fun `withDefaultSubtype replaces a wildcard subtype with a concrete one`() {
        assertThat(MimeTypes.Images.withDefaultSubtype()).isEqualTo(MimeTypes.Jpeg)
        assertThat(MimeTypes.Videos.withDefaultSubtype()).isEqualTo(MimeTypes.Mp4)
        assertThat(MimeTypes.Audio.withDefaultSubtype()).isEqualTo(MimeTypes.Mp3)
    }

    @Test
    fun `withDefaultSubtype falls back to octet stream`() {
        assertThat(MimeTypes.Any.withDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
        assertThat("text/*".withDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
        assertThat(null.withDefaultSubtype()).isEqualTo(MimeTypes.OctetStream)
    }
}
