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

package io.element.android.features.messages.impl.messagecomposer

import android.annotation.SuppressLint
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
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.core.data.toStableCharSequence
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.SnackbarMessage
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.textcomposer.MessageComposerMode
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.element.android.libraries.core.mimetype.MimeTypes.Any as AnyMimeTypes

@SingleIn(RoomScope::class)
class MessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val room: MatrixRoom,
    private val mediaPickerProvider: PickerProvider,
    private val featureFlagService: FeatureFlagService,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaSender: MediaSender,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContextImpl,
) : Presenter<MessageComposerState> {

    @SuppressLint("UnsafeOptInUsageError")
    @Composable
    override fun present(): MessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()

        val attachmentsState = remember {
            mutableStateOf<AttachmentsState>(AttachmentsState.None)
        }

        val galleryMediaPicker = mediaPickerProvider.registerGalleryPicker { uri, mimeType ->
            handlePickedMedia(attachmentsState, uri, mimeType)
        }
        val filesPicker = mediaPickerProvider.registerFilePicker(AnyMimeTypes) { uri ->
            handlePickedMedia(attachmentsState, uri, compressIfPossible = false)
        }
        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker { uri ->
            handlePickedMedia(attachmentsState, uri, MimeTypes.IMAGE_JPEG)
        }
        val cameraVideoPicker = mediaPickerProvider.registerCameraVideoPicker { uri ->
            handlePickedMedia(attachmentsState, uri, MimeTypes.VIDEO_MP4)
        }
        val isFullScreen = rememberSaveable {
            mutableStateOf(false)
        }
        val hasFocus = remember {
            mutableStateOf(false)
        }
        val text: MutableState<StableCharSequence> = remember {
            mutableStateOf(StableCharSequence(""))
        }

        var showAttachmentSourcePicker: Boolean by remember { mutableStateOf(false) }

        LaunchedEffect(messageComposerContext.composerMode) {
            when (val modeValue = messageComposerContext.composerMode) {
                is MessageComposerMode.Edit -> text.value = modeValue.defaultContent.toStableCharSequence()
                else -> Unit
            }
        }

        LaunchedEffect(attachmentsState.value) {
            when (val attachmentStateValue = attachmentsState.value) {
                is AttachmentsState.Sending.Processing -> localCoroutineScope.sendAttachment(attachmentStateValue.attachments.first(), attachmentsState)
                else -> Unit
            }
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value

                is MessageComposerEvents.FocusChanged -> hasFocus.value = event.hasFocus

                is MessageComposerEvents.UpdateText -> text.value = event.text.toStableCharSequence()
                MessageComposerEvents.CloseSpecialMode -> {
                    text.value = "".toStableCharSequence()
                    messageComposerContext.composerMode = MessageComposerMode.Normal("")
                }

                is MessageComposerEvents.SendMessage -> appCoroutineScope.sendMessage(
                    text = event.message,
                    updateComposerMode = { messageComposerContext.composerMode = it },
                    textState = text
                )
                is MessageComposerEvents.SetMode -> {
                    messageComposerContext.composerMode = event.composerMode
                    analyticsService.capture(
                        Composer(
                            inThread = messageComposerContext.composerMode.inThread,
                            isEditing = messageComposerContext.composerMode.isEditing,
                            isReply = messageComposerContext.composerMode.isReply,
                            isLocation = false,
                        )
                    )
                }
                MessageComposerEvents.AddAttachment -> localCoroutineScope.launchIfMediaPickerEnabled {
                    showAttachmentSourcePicker = true
                }
                MessageComposerEvents.DismissAttachmentMenu -> showAttachmentSourcePicker = false
                MessageComposerEvents.PickAttachmentSource.FromGallery -> localCoroutineScope.launchIfMediaPickerEnabled {
                    showAttachmentSourcePicker = false
                    galleryMediaPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.FromFiles -> localCoroutineScope.launchIfMediaPickerEnabled {
                    showAttachmentSourcePicker = false
                    filesPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.PhotoFromCamera -> localCoroutineScope.launchIfMediaPickerEnabled {
                    showAttachmentSourcePicker = false
                    cameraPhotoPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.VideoFromCamera -> localCoroutineScope.launchIfMediaPickerEnabled {
                    showAttachmentSourcePicker = false
                    cameraVideoPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.Location -> {
                    showAttachmentSourcePicker = false
                    // Navigation to the location picker screen is done at the view layer
                }
            }
        }

        return MessageComposerState(
            text = text.value,
            isFullScreen = isFullScreen.value,
            hasFocus = hasFocus.value,
            mode = messageComposerContext.composerMode,
            showAttachmentSourcePicker = showAttachmentSourcePicker,
            attachmentsState = attachmentsState.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.launchIfMediaPickerEnabled(action: suspend () -> Unit) = launch {
        if (featureFlagService.isFeatureEnabled(FeatureFlags.ShowMediaUploadingFlow)) {
            action()
        }
    }

    private fun CoroutineScope.sendMessage(
        text: String,
        updateComposerMode: (newComposerMode: MessageComposerMode) -> Unit,
        textState: MutableState<StableCharSequence>
    ) = launch {
        val capturedMode = messageComposerContext.composerMode
        // Reset composer right away
        textState.value = "".toStableCharSequence()
        updateComposerMode(MessageComposerMode.Normal(""))
        when (capturedMode) {
            is MessageComposerMode.Normal -> room.sendMessage(text)
            is MessageComposerMode.Edit -> {
                val eventId = capturedMode.eventId
                val transactionId = capturedMode.transactionId
                room.editMessage(eventId, transactionId, text)
            }

            is MessageComposerMode.Quote -> TODO()
            is MessageComposerMode.Reply -> room.replyMessage(
                capturedMode.eventId,
                text
            )
        }
    }

    private fun CoroutineScope.sendAttachment(
        attachment: Attachment,
        attachmentState: MutableState<AttachmentsState>,
    ) = launch {
        when (attachment) {
            is Attachment.Media -> {
                sendMedia(
                    uri = attachment.localMedia.uri,
                    mimeType = attachment.localMedia.info.mimeType,
                    attachmentState = attachmentState
                )
            }
        }
    }

    @UnstableApi
    private fun handlePickedMedia(
        attachmentsState: MutableState<AttachmentsState>,
        uri: Uri?,
        mimeType: String? = null,
        compressIfPossible: Boolean = true,
    ) {
        if (uri == null) {
            attachmentsState.value = AttachmentsState.None
            return
        }
        val localMedia = localMediaFactory.createFromUri(
            uri = uri,
            mimeType = mimeType,
            name = null,
            formattedFileSize = null
        )
        val mediaAttachment = Attachment.Media(localMedia, compressIfPossible)
        val isPreviewable = when {
            MimeTypes.isImage(localMedia.info.mimeType) -> true
            MimeTypes.isVideo(localMedia.info.mimeType) -> true
            MimeTypes.isAudio(localMedia.info.mimeType) -> true
            else -> false
        }
        attachmentsState.value = if (isPreviewable) {
            AttachmentsState.Previewing(persistentListOf(mediaAttachment))
        } else {
            AttachmentsState.Sending.Processing(persistentListOf(mediaAttachment))
        }
    }

    private suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        attachmentState: MutableState<AttachmentsState>,
    ) {
        val progressCallback = object : ProgressCallback {
            override fun onProgress(current: Long, total: Long) {
                attachmentState.value = AttachmentsState.Sending.Uploading(current.toFloat() / total.toFloat())
            }
        }
        mediaSender.sendMedia(uri, mimeType, compressIfPossible = false, progressCallback)
            .onSuccess {
                attachmentState.value = AttachmentsState.None
            }.onFailure {
                val snackbarMessage = SnackbarMessage(sendAttachmentError(it))
                snackbarDispatcher.post(snackbarMessage)
                attachmentState.value = AttachmentsState.None
            }
    }
}
