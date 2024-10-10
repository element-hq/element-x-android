/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.test.media.FakeMediaFile
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidLocalMediaFactoryTest {
    @Test
    fun `test AndroidLocalMediaFactory`() {
        val sut = createAndroidLocalMediaFactory()
        val result = sut.createFromMediaFile(aMediaFile(), anImageMediaInfo())
        assertThat(result.uri.toString()).endsWith("aPath")
        assertThat(result.info).isEqualTo(
            MediaInfo(
                filename = "an image file.jpg",
                caption = null,
                mimeType = MimeTypes.Jpeg,
                formattedFileSize = "4MB",
                fileExtension = "jpg",
            )
        )
    }

    private fun aMediaFile(): MediaFile {
        return FakeMediaFile("aPath")
    }

    private fun createAndroidLocalMediaFactory(): AndroidLocalMediaFactory {
        return AndroidLocalMediaFactory(
            RuntimeEnvironment.getApplication(),
            FakeFileSizeFormatter(),
            FileExtensionExtractorWithoutValidation()
        )
    }
}
