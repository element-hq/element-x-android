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
import androidx.compose.runtime.Composable

/**
 * Registers the system media and file pickers from Composable code, returning launchers the UI can trigger later.
 *
 * Every method must be called during composition, not from a click handler; a cancelled pick reports a `null` uri or an empty list.
 */
interface PickerProvider {
    /**
     * Registers a picker for a single photo or video from the gallery.
     *
     * @param onResult called with the picked media and its MIME type, both `null` when the user cancelled.
     */
    @Composable
    fun registerGalleryPicker(
        onResult: (uri: Uri?, mimeType: String?) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, Uri?>

    /**
     * Registers a picker restricted to a single image, used where a video would not be valid, such as for an avatar.
     *
     * @param onResult called with the picked image, `null` when the user cancelled.
     */
    @Composable
    fun registerGalleryImagePicker(
        onResult: (Uri?) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, Uri?>

    /**
     * Registers a picker for several photos or videos at once.
     *
     * @param onResult called with the picked media, empty when the user cancelled.
     */
    @Composable
    fun registerGalleryMultiPicker(
        onResult: (uris: List<Uri>) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, List<Uri>>

    /**
     * Registers a picker for a single file of any kind.
     *
     * @param mimeType the MIME type to filter on, `*` / `*` to accept anything.
     * @param onResult called with the picked file and its MIME type, both `null` when the user cancelled.
     */
    @Composable
    fun registerFilePicker(
        mimeType: String,
        onResult: (uri: Uri?, mimeType: String?) -> Unit,
    ): PickerLauncher<String, Uri?>

    /**
     * Registers a picker for several files at once.
     *
     * @param mimeType the MIME type to filter on.
     * @param onResult called with the picked files, empty when the user cancelled.
     */
    @Composable
    fun registerFileMultiPicker(
        mimeType: String,
        onResult: (uris: List<Uri>) -> Unit,
    ): PickerLauncher<Array<String>, List<Uri>>

    /**
     * Registers a launcher that opens the camera to take a photo.
     *
     * @param onResult called with the captured photo, `null` when the user cancelled or the capture failed.
     */
    @Composable
    fun registerCameraPhotoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean>

    /**
     * Registers a launcher that opens the camera to record a video.
     *
     * @param onResult called with the recorded video, `null` when the user cancelled or the capture failed.
     */
    @Composable
    fun registerCameraVideoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean>
}
