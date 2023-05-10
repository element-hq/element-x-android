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

package io.element.android.libraries.mediapickers.test

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.mediapickers.api.NoOpPickerLauncher
import io.element.android.libraries.mediapickers.api.PickerLauncher
import io.element.android.libraries.mediapickers.api.PickerProvider

class FakePickerProvider : PickerProvider {
    private var mimeType = MimeTypes.Any
    private var result: Uri? = null

    @Composable
    override fun registerGalleryPicker(onResult: (uri: Uri?, mimeType: String?) -> Unit): PickerLauncher<PickVisualMediaRequest, Uri?> {
        return NoOpPickerLauncher { onResult(result, mimeType) }
    }

    @Composable
    override fun registerFilePicker(mimeType: String, onResult: (Uri?) -> Unit): PickerLauncher<String, Uri?> {
        return NoOpPickerLauncher { onResult(result) }
    }

    @Composable
    override fun registerCameraPhotoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean> {
        return NoOpPickerLauncher { onResult(result) }
    }

    @Composable
    override fun registerCameraVideoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean> {
        return NoOpPickerLauncher { onResult(result) }
    }

    fun givenResult(value: Uri?) {
        this.result = value
    }

    fun givenMimeType(mimeType: String) {
        this.mimeType = mimeType
    }
}
