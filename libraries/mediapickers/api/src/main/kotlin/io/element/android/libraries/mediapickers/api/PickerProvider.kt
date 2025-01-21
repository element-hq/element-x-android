/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediapickers.api

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable

interface PickerProvider {
    @Composable
    fun registerGalleryPicker(
        onResult: (uri: Uri?, mimeType: String?) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, Uri?>

    @Composable
    fun registerGalleryImagePicker(
        onResult: (Uri?) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, Uri?>

    @Composable
    fun registerFilePicker(
        mimeType: String,
        onResult: (Uri?) -> Unit
    ): PickerLauncher<String, Uri?>

    @Composable
    fun registerCameraPhotoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean>

    @Composable
    fun registerCameraVideoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean>
}
