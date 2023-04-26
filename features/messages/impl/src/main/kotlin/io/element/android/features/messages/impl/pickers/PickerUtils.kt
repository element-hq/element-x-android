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

package io.element.android.features.messages.impl.pickers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import io.element.android.libraries.core.mimetype.MimeTypes

/**
 * Wrapper around [ManagedActivityResultLauncher] to be used with media/file pickers.
 */
interface PickerLauncher<Input, Output> {
    /** Starts the activity result launcher with its default input. */
    fun launch()

    /** Starts the activity result launcher with a [customInput]. */
    fun launch(customInput: Input)
}

class ComposePickerLauncher<Input, Output>(
    private val managedLauncher: ManagedActivityResultLauncher<Input, Output>,
    private val defaultRequest: Input,
) : PickerLauncher<Input, Output> {
    override fun launch() {
        managedLauncher.launch(defaultRequest)
    }

    override fun launch(customInput: Input) {
        managedLauncher.launch(customInput)
    }
}

/** Needed for screenshot tests. */
class NoOpPickerLauncher<Input, Output> : PickerLauncher<Input, Output> {
    override fun launch() {}
    override fun launch(customInput: Input) {}
}

sealed interface PickerType<Input, Output> {
    fun getContract(): ActivityResultContract<Input, Output>
    fun getDefaultRequest(): Input

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

    object File : PickerType<String, Uri?> {
        override fun getContract() = ActivityResultContracts.GetContent()
        override fun getDefaultRequest(): String {
            return MimeTypes.Any
        }
    }
}

class CameraContract : ActivityResultContracts.CaptureVideo() {
    override fun createIntent(context: Context, input: Uri): Intent {
        super.createIntent(context, input)
        return Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
    }
}

/**
 * Remembers and returns a [PickerLauncher] for a certain media/file [type].
 */
@Composable
fun <Input, Output> rememberPickerLauncher(
    type: PickerType<Input, Output>,
    onResult: (Output) -> Unit,
): PickerLauncher<Input, Output> {
    return if (LocalInspectionMode.current) {
        NoOpPickerLauncher()
    } else {
        val contract = type.getContract()
        val managedLauncher = rememberLauncherForActivityResult(contract = contract, onResult = onResult)
        remember(type) { ComposePickerLauncher(managedLauncher, type.getDefaultRequest()) }
    }
}
