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

package io.element.android.libraries.mediapickers.impl

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.FileProvider
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.mediapickers.api.ComposePickerLauncher
import io.element.android.libraries.mediapickers.api.NoOpPickerLauncher
import io.element.android.libraries.mediapickers.api.PickerLauncher
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediapickers.api.PickerType
import java.io.File
import java.util.UUID
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class PickerProviderImpl(private val isInTest: Boolean) : PickerProvider {

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
     * Remembers and returns a [PickerLauncher] for a gallery picture.
     * [onResult] will be called with either the selected file's [Uri] or `null` if nothing was selected.
     */
    @Composable
    override fun registerGalleryImagePicker(onResult: (Uri?) -> Unit): PickerLauncher<PickVisualMediaRequest, Uri?> {
        // Tests and UI preview can't handle Contexts, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            rememberPickerLauncher(type = PickerType.Image) { uri -> onResult(uri) }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for a gallery item, either a picture or a video.
     * [onResult] will be called with either the selected file's [Uri] or `null` if nothing was selected.
     */
    @Composable
    override fun registerGalleryPicker(
        onResult: (uri: Uri?, mimeType: String?) -> Unit
    ): PickerLauncher<PickVisualMediaRequest, Uri?> {
        // Tests and UI preview can't handle Contexts, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null, null) }
        } else {
            val context = LocalContext.current
            rememberPickerLauncher(type = PickerType.ImageAndVideo) { uri ->
                val mimeType = uri?.let { context.contentResolver.getType(it) }
                onResult(uri, mimeType)
            }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for a file of a certain [mimeType] (any type of file, by default).
     * [onResult] will be called with either the selected file's [Uri] or `null` if nothing was selected.
     */
    @Composable
    override fun registerFilePicker(
        mimeType: String,
        onResult: (Uri?) -> Unit,
    ): PickerLauncher<String, Uri?> {
        // Tests and UI preview can't handle Context or FileProviders, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            rememberPickerLauncher(type = PickerType.File(mimeType)) { uri -> onResult(uri) }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for taking a photo with a camera app.
     * @param [onResult] will be called with either the photo's [Uri] or `null` if nothing was selected.
     */
    @Composable
    override fun registerCameraPhotoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean> {
        // Tests and UI preview can't handle Context or FileProviders, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            val context = LocalContext.current
            val tmpFile = remember { getTemporaryFile(context) }
            val tmpFileUri = remember(tmpFile) { getTemporaryUri(context, tmpFile) }
            rememberPickerLauncher(type = PickerType.Camera.Photo(tmpFileUri)) { success ->
                // Execute callback
                onResult(if (success) tmpFileUri else null)
            }
        }
    }

    /**
     * Remembers and returns a [PickerLauncher] for recording a video with a camera app.
     * @param [onResult] will be called with either the video's [Uri] or `null` if nothing was selected.
     */
    @Composable
    override fun registerCameraVideoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean> {
        // Tests and UI preview can't handle Context or FileProviders, so we might as well disable the whole picker
        return if (LocalInspectionMode.current || isInTest) {
            NoOpPickerLauncher { onResult(null) }
        } else {
            val context = LocalContext.current
            val tmpFile = remember { getTemporaryFile(context) }
            val tmpFileUri = remember(tmpFile) { getTemporaryUri(context, tmpFile) }
            rememberPickerLauncher(type = PickerType.Camera.Video(tmpFileUri)) { success ->
                // Execute callback
                onResult(if (success) tmpFileUri else null)
            }
        }
    }

    private fun getTemporaryFile(
        context: Context,
        baseFolder: File = context.cacheDir,
        filename: String = UUID.randomUUID().toString(),
    ): File {
        return File(baseFolder, filename)
    }

    private fun getTemporaryUri(
        context: Context,
        file: File,
    ): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
