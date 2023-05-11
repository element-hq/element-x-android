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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.core.data.toStableCharSequence
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.SnackbarMessage
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaType
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import io.element.android.libraries.ui.strings.R as StringR

@SingleIn(RoomScope::class)
class MessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val room: MatrixRoom,
    private val mediaPickerProvider: PickerProvider,
    private val featureFlagService: FeatureFlagService,
    private val mediaPreProcessor: MediaPreProcessor,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<MessageComposerState> {

    @Composable
    override fun present(): MessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()

        val galleryMediaPicker = mediaPickerProvider.registerGalleryPicker(onResult = { uri, mimeType ->
            if (uri == null) return@registerGalleryPicker
            Timber.d("Media picked from $uri")
            // We don't know which type of media was retrieved, so we need this check
            val mediaType = when {
                mimeType.isMimeTypeImage() -> MediaType.Image
                mimeType.isMimeTypeVideo() -> MediaType.Video
                else -> error("MimeType must be either image/* or video/*")
            }
            appCoroutineScope.sendMedia(uri, mediaType)
        })

        val filesPicker = mediaPickerProvider.registerFilePicker(mimeType = MimeTypes.Any) { uri ->
            if (uri == null) return@registerFilePicker
            Timber.d("File picked from $uri")
            appCoroutineScope.sendMedia(uri, MediaType.File)
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker { uri ->
            if (uri == null) return@registerCameraPhotoPicker
            Timber.d("Photo saved at $uri")
            appCoroutineScope.sendMedia(uri, MediaType.Image, deleteOriginal = true)
        }

        val cameraVideoPicker = mediaPickerProvider.registerCameraVideoPicker { uri ->
            if (uri == null) return@registerCameraVideoPicker
            Timber.d("Video saved at $uri")
            appCoroutineScope.sendMedia(uri, MediaType.Video, deleteOriginal = true)
        }

        val isFullScreen = rememberSaveable {
            mutableStateOf(false)
        }
        val text: MutableState<StableCharSequence> = remember {
            mutableStateOf(StableCharSequence(""))
        }
        val composerMode: MutableState<MessageComposerMode> = rememberSaveable {
            mutableStateOf(MessageComposerMode.Normal(""))
        }

        var attachmentSourcePicker: AttachmentSourcePicker? by remember { mutableStateOf(null) }

        LaunchedEffect(composerMode.value) {
            when (val modeValue = composerMode.value) {
                is MessageComposerMode.Edit -> text.value = modeValue.defaultContent.toStableCharSequence()
                else -> Unit
            }
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value
                is MessageComposerEvents.UpdateText -> text.value = event.text.toStableCharSequence()
                MessageComposerEvents.CloseSpecialMode -> {
                    text.value = "".toStableCharSequence()
                    composerMode.setToNormal()
                }

                is MessageComposerEvents.SendMessage -> appCoroutineScope.sendMessage(event.message, composerMode, text)
                is MessageComposerEvents.SetMode -> composerMode.value = event.composerMode
                MessageComposerEvents.AddAttachment -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = AttachmentSourcePicker.AllMedia
                }
                MessageComposerEvents.DismissAttachmentMenu -> attachmentSourcePicker = null
                MessageComposerEvents.PickAttachmentSource.FromGallery -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = null
                    galleryMediaPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.FromFiles -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = null
                    filesPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.FromCamera -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = AttachmentSourcePicker.Camera
                }
                MessageComposerEvents.PickCameraAttachmentSource.Photo -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = null
                    cameraPhotoPicker.launch()
                }
                MessageComposerEvents.PickCameraAttachmentSource.Video -> localCoroutineScope.ifMediaPickersEnabled {
                    attachmentSourcePicker = null
                    cameraVideoPicker.launch()
                }
            }
        }

        return MessageComposerState(
            text = text.value,
            isFullScreen = isFullScreen.value,
            mode = composerMode.value,
            attachmentSourcePicker = attachmentSourcePicker,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.ifMediaPickersEnabled(action: suspend () -> Unit) = launch {
        if (featureFlagService.isFeatureEnabled(FeatureFlags.ShowMediaUploadingFlow)) {
            action()
        }
    }

    private fun MutableState<MessageComposerMode>.setToNormal() {
        value = MessageComposerMode.Normal("")
    }

    private fun CoroutineScope.sendMessage(text: String, composerMode: MutableState<MessageComposerMode>, textState: MutableState<StableCharSequence>) =
        launch {
            val capturedMode = composerMode.value
            // Reset composer right away
            textState.value = "".toStableCharSequence()
            composerMode.setToNormal()
            when (capturedMode) {
                is MessageComposerMode.Normal -> room.sendMessage(text)
                is MessageComposerMode.Edit -> room.editMessage(
                    capturedMode.eventId,
                    text
                )

                is MessageComposerMode.Quote -> TODO()
                is MessageComposerMode.Reply -> room.replyMessage(
                    capturedMode.eventId,
                    text
                )
            }
        }

    private fun CoroutineScope.sendMedia(
        uri: Uri,
        mediaType: MediaType,
        deleteOriginal: Boolean = false
    ) = launch {
        runCatching {
            val info = handleMediaPreProcessing(uri, mediaType, deleteOriginal).getOrNull() ?: return@runCatching
            when (info) {
                is MediaUploadInfo.Image -> {
                    room.sendImage(info.file, info.thumbnailInfo.file, info.info)
                }

                is MediaUploadInfo.Video -> {
                    room.sendVideo(info.file, info.thumbnailInfo.file, info.info)
                }

                is MediaUploadInfo.AnyFile -> {
                    room.sendFile(info.file, info.info)
                }
                else -> error("Unexpected MediaUploadInfo format: $info")
            }.getOrThrow()
        }.onFailure {
            snackbarDispatcher.post(SnackbarMessage(StringR.string.screen_media_upload_preview_error_failed_sending))
            Timber.e(it, "Couldn't upload media")
        }.onSuccess {
            Timber.d("Media uploaded")
        }
    }

    private suspend fun handleMediaPreProcessing(
        uri: Uri,
        mediaType: MediaType,
        deleteOriginal: Boolean,
    ): Result<MediaUploadInfo> {
        val result = mediaPreProcessor.process(uri, mediaType, deleteOriginal = deleteOriginal)
        Timber.d("Pre-processed media result: $result")
        return result.onFailure {
            snackbarDispatcher.post(SnackbarMessage(StringR.string.screen_media_upload_preview_error_failed_processing))
        }
    }
}
