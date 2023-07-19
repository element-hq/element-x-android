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

package io.element.android.libraries.mediapickers.api

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import io.element.android.libraries.core.mimetype.MimeTypes

sealed interface PickerType<Input, Output> {
    fun getContract(): ActivityResultContract<Input, Output>
    fun getDefaultRequest(): Input

    object Image : PickerType<PickVisualMediaRequest, Uri?> {
        override fun getContract() = ActivityResultContracts.PickVisualMedia()
        override fun getDefaultRequest(): PickVisualMediaRequest {
            return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }
    }

    object ImageAndVideo : PickerType<PickVisualMediaRequest, Uri?> {
        override fun getContract() = ActivityResultContracts.PickVisualMedia()
        override fun getDefaultRequest(): PickVisualMediaRequest {
            return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        }
    }

    object Camera {
        data class Photo(val destUri: Uri) : PickerType<Uri, Boolean> {
            override fun getContract() = ActivityResultContracts.TakePicture()
            override fun getDefaultRequest(): Uri {
                return destUri
            }
        }

        data class Video(val destUri: Uri) : PickerType<Uri, Boolean> {
            override fun getContract() = ActivityResultContracts.CaptureVideo()
            override fun getDefaultRequest(): Uri {
                return destUri
            }
        }
    }

    data class File(val mimeType: String = MimeTypes.Any) : PickerType<String, Uri?> {
        override fun getContract() = ActivityResultContracts.GetContent()
        override fun getDefaultRequest(): String {
            return mimeType
        }
    }
}
