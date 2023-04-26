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

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.element.android.libraries.core.mimetype.MimeTypes

/**
 * Wrapper around [ManagedActivityResultLauncher] to be used with media/file pickers.
 */
class PickerLauncher<Input, Output>(
    private val managedLauncher: ManagedActivityResultLauncher<Input, Output?>,
    private val defaultRequest: Input,
) {
    /** Starts the activity result launcher with its default input. */
    fun launch() {
        managedLauncher.launch(defaultRequest)
    }

    /** Starts the activity result launcher with a [customInput]. */
    fun launch(customInput: Input) {
        managedLauncher.launch(customInput)
    }
}

sealed interface PickerType<Input, Output> {
    fun getContract(): ActivityResultContract<Input, Output?>
    fun getDefaultRequest(): Input

    object Image : PickerType<PickVisualMediaRequest, Uri> {
        override fun getContract() = ActivityResultContracts.PickVisualMedia()
        override fun getDefaultRequest(): PickVisualMediaRequest {
            return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }
    }

    object Video : PickerType<PickVisualMediaRequest, Uri> {
        override fun getContract() = ActivityResultContracts.PickVisualMedia()
        override fun getDefaultRequest(): PickVisualMediaRequest {
            return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }
    }

    object Audio : PickerType<String, Uri> {
        override fun getContract() = ActivityResultContracts.GetContent()
        override fun getDefaultRequest(): String {
            return MimeTypes.Audio
        }
    }

    object File : PickerType<String, Uri> {
        override fun getContract() = ActivityResultContracts.GetContent()
        override fun getDefaultRequest(): String {
            return MimeTypes.Any
        }
    }
}

/**
 * Remembers and returns a [PickerLauncher] for a certain media/file [type].
 * If the picker returns an item, it'll be emitted in [onSuccess]. If it does not, [onFailure] will be called instead.
 */
@Composable
fun <Input, Output> rememberPickerLauncher(
    type: PickerType<Input, Output>,
    onSuccess: (Output) -> Unit,
    onFailure: () -> Unit = {},
): PickerLauncher<Input, Output> {
    val contract = type.getContract()
    val managedLauncher = rememberLauncherForActivityResult(contract = contract, onResult = { result ->
        if (result != null) {
            onSuccess(result)
        } else {
            onFailure()
        }
    })

    return remember { PickerLauncher(managedLauncher, type.getDefaultRequest()) }
}
