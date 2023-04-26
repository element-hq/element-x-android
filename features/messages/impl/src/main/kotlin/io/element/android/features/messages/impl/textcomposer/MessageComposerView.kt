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

package io.element.android.features.messages.impl.textcomposer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.content.FileProvider
import io.element.android.features.messages.impl.pickers.NoOpPickerLauncher
import io.element.android.features.messages.impl.pickers.PickerLauncher
import io.element.android.features.messages.impl.pickers.PickerType
import io.element.android.features.messages.impl.pickers.rememberPickerLauncher
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.textcomposer.TextComposer
import timber.log.Timber
import java.io.File
import java.util.UUID

@Composable
fun MessageComposerView(
    state: MessageComposerState,
    modifier: Modifier = Modifier,
) {
    // Example usage of custom pickers
    val cameraLauncher = CameraPhotoPicker(onResult = { uri ->
        Timber.d("Photo saved at $uri")
    })

    fun onFullscreenToggle() {
        state.eventSink(MessageComposerEvents.ToggleFullScreenState)
    }

    fun sendMessage(message: String) {
        state.eventSink(MessageComposerEvents.SendMessage(message))
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvents.CloseSpecialMode)
    }

    fun onComposerTextChange(text: CharSequence) {
        state.eventSink(MessageComposerEvents.UpdateText(text))
    }

    TextComposer(
        onSendMessage = ::sendMessage,
        fullscreen = state.isFullScreen,
        onFullscreenToggle = ::onFullscreenToggle,
        composerMode = state.mode,
        onCloseSpecialMode = ::onCloseSpecialMode,
        onComposerTextChange = ::onComposerTextChange,
        onAddAttachment = {
            cameraLauncher.launch()
        },
        composerCanSendMessage = state.isSendButtonVisible,
        composerText = state.text?.charSequence?.toString(),
        isInDarkMode = !ElementTheme.colors.isLight,
        modifier = modifier
    )
}

@Composable
internal fun CameraPhotoPicker(onResult: (Uri?) -> Unit): PickerLauncher<Uri, Boolean> {
    // Screenshot tests and preview can't handle FileProviders, so we might as well disable the whole picker
    return if (LocalInspectionMode.current) {
        NoOpPickerLauncher()
    } else {
        val context = LocalContext.current
        val tmpFile = remember {
            val filename = UUID.randomUUID().toString()
            File(context.cacheDir, filename)
        }
        val tmpFileUri = remember(tmpFile) {
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, tmpFile)
        }
        rememberPickerLauncher(type = PickerType.Camera.Photo(tmpFileUri)) { success ->
            // Execute callback
            onResult(if (success) tmpFileUri else null)
            // Then remove the file and clear the picker
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
    }
}

@Preview
@Composable
internal fun MessageComposerViewLightPreview(@PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun MessageComposerViewDarkPreview(@PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MessageComposerState) {
    MessageComposerView(state)
}
