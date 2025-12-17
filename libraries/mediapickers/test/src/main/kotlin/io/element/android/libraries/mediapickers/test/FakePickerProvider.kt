/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
    override fun registerGalleryImagePicker(onResult: (uri: Uri?) -> Unit): PickerLauncher<PickVisualMediaRequest, Uri?> {
        return NoOpPickerLauncher { onResult(result) }
    }

    @Composable
    override fun registerFilePicker(mimeType: String, onResult: (Uri?, String?) -> Unit): PickerLauncher<String, Uri?> {
        return NoOpPickerLauncher { onResult(result, this.mimeType) }
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
