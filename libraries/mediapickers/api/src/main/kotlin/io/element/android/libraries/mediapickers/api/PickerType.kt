/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediapickers.api

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Immutable
import io.element.android.libraries.core.mimetype.MimeTypes

@Immutable
sealed interface PickerType<Input, Output> {
    fun getContract(): ActivityResultContract<Input, Output>
    fun getDefaultRequest(): Input

    data object Image : PickerType<PickVisualMediaRequest, Uri?> {
        override fun getContract() = ActivityResultContracts.PickVisualMedia()
        override fun getDefaultRequest(): PickVisualMediaRequest {
            return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }
    }

    data object ImageAndVideo : PickerType<PickVisualMediaRequest, Uri?> {
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
