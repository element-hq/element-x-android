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

class DefaultAttachmentImageEditorTest {
    @Test
    fun `exported mime type preserves png`() {
        assertThat(exportedMimeTypeFor(MimeTypes.Png)).isEqualTo(MimeTypes.Png)
    }

    @Test
    fun `exported mime type normalizes non-png images to jpeg`() {
        assertThat(exportedMimeTypeFor("image/heic")).isEqualTo(MimeTypes.Jpeg)
    }
}
