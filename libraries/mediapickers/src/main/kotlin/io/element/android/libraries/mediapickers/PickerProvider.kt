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

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.FileProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import java.io.File
import java.util.UUID
import javax.inject.Inject

class PickerProvider constructor(private val isInTest: Boolean) {

    @Inject
    constructor(): this(false)

    /**
     * Remembers and returns a [PickerLauncher] for a certain media/file [type].
     */
    @Composable
    internal fun <Input, Output> rememberPickerLauncher(
        type: PickerType<Input, Output>,
        onResult: (Output) -> Unit,
    ): PickerLauncher<Input, Output> {
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { }
        } else {
            val contract = type.getContract()
            val managedLauncher = rememberLauncherForActivityResult(contract = contract, onResult = onResult)
            remember(type) { ComposePickerLauncher(managedLauncher, type.getDefaultRequest()) }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for a gallery item, either a picture or a video.
     * [onResult] will be called with either the selected file's [Uri] or `null` if nothing was selected.
     */
    @Composable
    fun registerGalleryPicker(onResult: (Uri?) -> Unit): PickerLauncher<PickVisualMediaRequest, Uri?> {
        // Tests and UI preview can't handle Contexts, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            rememberPickerLauncher(type = PickerType.ImageAndVideo) { uri -> onResult(uri) }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for a file of a certain [mimeType] (any type of file, by default).
     * [onResult] will be called with either the selected file's [Uri] or `null` if nothing was selected.
     */
    @Composable
    fun registerFilePicker(mimeType: String = MimeTypes.Any, onResult: (Uri?) -> Unit): PickerLauncher<String, Uri?> {
        // Tests and UI preview can't handle Contexts, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            rememberPickerLauncher(type = PickerType.File(mimeType)) { uri -> onResult(uri) }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for taking a photo with a camera app.
     * @param [onResult] will be called with either the photo's [Uri] or `null` if nothing was selected.
     * @param [deleteAfter] When it's `true`, the taken photo will be automatically removed after calling [onResult].
     *  It's `true` by default.
     */
    @Composable
    fun registerCameraPhotoPicker(onResult: (Uri?) -> Unit, deleteAfter: Boolean = true): PickerLauncher<Uri, Boolean> {
        // Tests and UI preview can't handle Context or FileProviders, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            val context = LocalContext.current
            val tmpFileUri = remember { getTemporaryUri(context) }
            rememberPickerLauncher(type = PickerType.Camera.Photo(tmpFileUri)) { success ->
                // Execute callback
                onResult(if (success) tmpFileUri else null)
                // Then remove the file and clear the picker
                if (deleteAfter) {
                    deleteTemporaryUri(tmpFileUri)
                }
            }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for recording a video with a camera app.
     * @param [onResult] will be called with either the video's [Uri] or `null` if nothing was selected.
     * @param [deleteAfter] When it's `true`, the recorded video will be automatically removed after calling [onResult].
     *  It's `true` by default.
     */
    @Composable
    fun registerCameraVideoPicker(onResult: (Uri?) -> Unit, deleteAfter: Boolean = true): PickerLauncher<Uri, Boolean> {
        // Tests and UI preview can't handle Context or FileProviders, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            val context = LocalContext.current
            val tmpFileUri = remember { getTemporaryUri(context) }
            rememberPickerLauncher(type = PickerType.Camera.Video(tmpFileUri)) { success ->
                // Execute callback
                onResult(if (success) tmpFileUri else null)
                // Then remove the file and clear the picker
                if (deleteAfter) {
                    deleteTemporaryUri(tmpFileUri)
                }
            }
        }
    }

    private fun getTemporaryUri(
        context: Context,
        baseFolder: File = context.cacheDir,
        filename: String = UUID.randomUUID().toString(),
    ): Uri {
        val tmpFile = File(baseFolder, filename)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tmpFile)
    }

    private fun deleteTemporaryUri(uri: Uri): Boolean {
        val provider = FileProvider()
        return provider.delete(uri, null, null) == 1
    }
}
