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
