/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediapickers

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.mediapickers.api.PickerType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PickerTypeTest {
    @Test
    fun `ImageAndVideo - assert types`() {
        val pickerType = PickerType.ImageAndVideo
        assertThat(pickerType.getContract()).isInstanceOf(ActivityResultContracts.PickVisualMedia::class.java)
        assertThat(pickerType.getDefaultRequest().mediaType).isEqualTo(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
    }

    @Test
    fun `File - assert types`() {
        val pickerType = PickerType.File()
        assertThat(pickerType.getContract()).isInstanceOf(ActivityResultContracts.GetContent::class.java)
        assertThat(pickerType.getDefaultRequest()).isEqualTo(MimeTypes.Any)

        val mimeType = MimeTypes.Images
        val customPickerType = PickerType.File(mimeType)
        assertThat(customPickerType.getContract()).isInstanceOf(ActivityResultContracts.GetContent::class.java)
        assertThat(customPickerType.getDefaultRequest()).isEqualTo(mimeType)
    }

    @Test
    fun `CameraPhoto - assert types`() {
        val uri = Uri.parse("file:///tmp/test")
        val pickerType = PickerType.Camera.Photo(uri)
        assertThat(pickerType.getContract()).isInstanceOf(ActivityResultContracts.TakePicture::class.java)
        assertThat(pickerType.getDefaultRequest()).isEqualTo(uri)
    }

    @Test
    fun `CameraVideo - assert types`() {
        val uri = Uri.parse("file:///tmp/test")
        val pickerType = PickerType.Camera.Video(uri)
        assertThat(pickerType.getContract()).isInstanceOf(ActivityResultContracts.CaptureVideo::class.java)
        assertThat(pickerType.getDefaultRequest()).isEqualTo(uri)
    }
}
