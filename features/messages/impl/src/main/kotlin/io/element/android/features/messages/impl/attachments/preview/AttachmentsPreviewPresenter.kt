/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEditor
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEdits
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEditorState
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorPresenter
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.firstInstanceOf
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.MediaSenderFactory
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.api.allFiles
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.rememberMarkdownTextEditorState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import timber.log.Timber

@AssistedInject
class AttachmentsPreviewPresenter(
    @Assisted private val attachment: Attachment,
    @Assisted private val onDoneListener: OnDoneListener,
    @Assisted private val timelineMode: Timeline.Mode,
    @Assisted private val inReplyToEventId: EventId?,
    mediaSenderFactory: MediaSenderFactory,
    private val permalinkBuilder: PermalinkBuilder,
    private val temporaryUriDeleter: TemporaryUriDeleter,
    private val attachmentImageEditor: AttachmentImageEditor,
    private val mediaOptimizationSelectorPresenterFactory: MediaOptimizationSelectorPresenter.Factory,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
) : Presenter<AttachmentsPreviewState> {
    @AssistedFactory
    interface Factory {
        fun create(
            attachment: Attachment,
            timelineMode: Timeline.Mode,
            onDoneListener: OnDoneListener,
            inReplyToEventId: EventId?,
        ): AttachmentsPreviewPresenter
    }

    private val mediaSender = mediaSenderFactory.create(timelineMode)

    @Composable
    override fun present(): AttachmentsPreviewState {
        val coroutineScope = rememberCoroutineScope()

        val sendActionState = remember {
            mutableStateOf<SendActionState>(SendActionState.Idle)
        }
        val originalLocalMedia = remember { (attachment as Attachment.Media).localMedia }
        var currentAttachment by remember { mutableStateOf(attachment) }
        var canEditImage by remember { mutableStateOf(originalLocalMedia.info.canEditImage()) }
        var imageEditorState by remember { mutableStateOf<AttachmentImageEditorState?>(null) }
        var appliedImageEdits by remember { mutableStateOf(AttachmentImageEdits()) }
        var isApplyingImageEdits by remember { mutableStateOf(false) }
        var displayImageEditError by remember { mutableStateOf(false) }
        var editedTempFile by remember { mutableStateOf<File?>(null) }

        val markdownTextEditorState = rememberMarkdownTextEditorState(initialText = null, initialFocus = false)
        val textEditorState by rememberUpdatedState(
            TextEditorState.Markdown(markdownTextEditorState, isRoomEncrypted = null)
        )

        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        var preprocessMediaJob by remember { mutableStateOf<Job?>(null) }

        val mediaAttachment = currentAttachment as Attachment.Media
        val mediaOptimizationSelectorPresenter = remember {
            mediaOptimizationSelectorPresenterFactory.create(mediaAttachment.localMedia)
        }
        val mediaOptimizationSelectorState by rememberUpdatedState(mediaOptimizationSelectorPresenter.present())

        val observableSendState = snapshotFlow { sendActionState.value }

        var displayFileTooLargeError by remember { mutableStateOf(false) }

        LaunchedEffect(mediaOptimizationSelectorState.displayMediaSelectorViews, currentAttachment, imageEditorState, isApplyingImageEdits) {
            // If the media optimization selector is not displayed, we can pre-process the media
            // to prepare it for sending. This is done to avoid blocking the UI thread when the
            // user clicks on the send button.
            if (mediaOptimizationSelectorState.displayMediaSelectorViews == false && imageEditorState == null && !isApplyingImageEdits) {
                preprocessMediaJob = preProcessAttachment(
                    attachment = currentAttachment,
                    mediaOptimizationConfig = mediaOptimizationConfigProvider.get(),
                    displayProgress = false,
                    sendActionState = sendActionState,
                )
            }
        }

        LaunchedEffect(originalLocalMedia) {
            canEditImage = originalLocalMedia.info.canEditImage() || attachmentImageEditor.canEdit(originalLocalMedia)
        }

        val maxUploadSize = mediaOptimizationSelectorState.maxUploadSize.dataOrNull()
        LaunchedEffect(maxUploadSize) {
            // Check file upload size if the media won't be processed for upload
            val isImageFile = mediaAttachment.localMedia.info.isImageAttachment()
            val isVideoFile = mediaAttachment.localMedia.info.mimeType.isMimeTypeVideo()
            if (maxUploadSize != null && !(isImageFile || isVideoFile)) {
                // If file size is not known, we're permissive and allow sending. The SDK will cancel the upload if needed.
                val fileSize = mediaAttachment.localMedia.info.fileSize ?: 0L
                if (maxUploadSize < fileSize) {
                    displayFileTooLargeError = true
                }
            }
        }

        val videoSizeEstimations = mediaOptimizationSelectorState.videoSizeEstimations.dataOrNull()
        LaunchedEffect(videoSizeEstimations) {
            if (videoSizeEstimations != null) {
                // Check if the video size estimations are too large for the max upload size
                displayFileTooLargeError = videoSizeEstimations.none { it.canUpload }
            }
        }

        fun handleEvent(event: AttachmentsPreviewEvent) {
            when (event) {
                is AttachmentsPreviewEvent.SendAttachment -> {
                    ongoingSendAttachmentJob.value = coroutineScope.launch {
                        // If the media optimization selector is displayed, we need to wait for the user to select the options
                        // before we can pre-process the media.
                        if (mediaOptimizationSelectorState.displayMediaSelectorViews == true) {
                            val config = MediaOptimizationConfig(
                                compressImages = mediaOptimizationSelectorState.isImageOptimizationEnabled == true,
                                videoCompressionPreset = mediaOptimizationSelectorState.selectedVideoPreset ?: VideoCompressionPreset.STANDARD,
                            )
                            preprocessMediaJob = preProcessAttachment(
                                attachment = currentAttachment,
                                mediaOptimizationConfig = config,
                                displayProgress = true,
                                sendActionState = sendActionState,
                            )
                        }

                        // If the processing was hidden before, make it visible now
                        if (sendActionState.value is SendActionState.Sending.Processing) {
                            sendActionState.value = SendActionState.Sending.Processing(displayProgress = true)
                        }

                        // Wait until the media is ready to be uploaded
                        val mediaUploadInfo = observableSendState.firstInstanceOf<SendActionState.Sending.ReadyToUpload>().mediaInfo

                        // Pre-processing is done, send the attachment
                        val caption = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
                            .takeIf { it.isNotEmpty() }

                        val editedTempFileToDelete = editedTempFile
                        editedTempFile = null

                        // If we're supposed to send the media as a background job, we can dismiss this screen already
                        if (coroutineContext.isActive) {
                            onDoneListener()
                        }

                        // Send the media using the session coroutine scope so it doesn't matter if this screen or the chat one are closed
                        sessionCoroutineScope.launch(dispatchers.io) {
                            try {
                                sendPreProcessedMedia(
                                    mediaUploadInfo = mediaUploadInfo,
                                    caption = caption,
                                    sendActionState = sendActionState,
                                    dismissAfterSend = false,
                                    inReplyToEventId = inReplyToEventId,
                                )
                            } finally {
                                editedTempFileToDelete?.safeDelete()
                                // Clean up the pre-processed media after it's been sent
                                mediaSender.cleanUp()
                            }
                        }
                    }
                }
                AttachmentsPreviewEvent.CancelAndDismiss -> {
                    displayFileTooLargeError = false
                    displayImageEditError = false
                    isApplyingImageEdits = false

                    // Cancel media preprocessing and sending
                    preprocessMediaJob?.cancel()
                    preprocessMediaJob = null
                    // If we couldn't send the pre-processed media, remove it
                    mediaSender.cleanUp()
                    ongoingSendAttachmentJob.value?.cancel()

                    // Dismiss the screen
                    dismiss(sendActionState, editedTempFile)
                }
                AttachmentsPreviewEvent.CancelAndClearSendState -> {
                    // Cancel media sending
                    ongoingSendAttachmentJob.value?.let {
                        it.cancel()
                        ongoingSendAttachmentJob.value = null
                    }

                    val mediaUploadInfo = sendActionState.value.mediaUploadInfo()
                    sendActionState.value = if (mediaUploadInfo != null) {
                        SendActionState.Sending.ReadyToUpload(mediaUploadInfo)
                    } else {
                        SendActionState.Idle
                    }
                }
                AttachmentsPreviewEvent.OpenImageEditor -> {
                    val resolvedCanEditImage = canEditImage || originalLocalMedia.info.canEditImage()
                    if (resolvedCanEditImage) {
                        preprocessMediaJob?.cancel()
                        preprocessMediaJob = null
                        resetPreparedMedia(sendActionState)
                        imageEditorState = AttachmentImageEditorState(
                            localMedia = originalLocalMedia,
                            edits = appliedImageEdits,
                        )
                    }
                }
                AttachmentsPreviewEvent.CloseImageEditor -> {
                    imageEditorState = null
                }
                is AttachmentsPreviewEvent.UpdateImageCropRect -> {
                    val pendingState = imageEditorState ?: return
                    imageEditorState = pendingState.copy(
                        edits = pendingState.edits.copy(cropRect = event.cropRect)
                    )
                }
                AttachmentsPreviewEvent.RotateImage -> {
                    val pendingState = imageEditorState ?: return
                    imageEditorState = pendingState.copy(
                        edits = pendingState.edits.rotateClockwise()
                    )
                }
                AttachmentsPreviewEvent.ApplyImageEdits -> {
                    val pendingState = imageEditorState ?: return
                    if (!pendingState.edits.hasChanges) {
                        editedTempFile?.safeDelete()
                        editedTempFile = null
                        appliedImageEdits = pendingState.edits
                        currentAttachment = Attachment.Media(originalLocalMedia)
                        imageEditorState = null
                        resetPreparedMedia(sendActionState)
                        return
                    }
                    isApplyingImageEdits = true
                    displayImageEditError = false
                    coroutineScope.launch {
                        val result = withContext(dispatchers.io) {
                            attachmentImageEditor.exportEdits(
                                localMedia = originalLocalMedia,
                                edits = pendingState.edits,
                            )
                        }
                        result.fold(
                            onSuccess = { editedMedia ->
                                editedTempFile?.safeDelete()
                                editedTempFile = editedMedia.file
                                appliedImageEdits = pendingState.edits
                                currentAttachment = Attachment.Media(editedMedia.localMedia)
                                imageEditorState = null
                                resetPreparedMedia(sendActionState)
                            },
                            onFailure = {
                                Timber.e(it, "Failed to apply image edits")
                                displayImageEditError = true
                            }
                        )
                        isApplyingImageEdits = false
                    }
                }
                AttachmentsPreviewEvent.ClearImageEditError -> {
                    displayImageEditError = false
                }
            }
        }

        return AttachmentsPreviewState(
            attachment = currentAttachment,
            imageEditorState = imageEditorState,
            canEditImage = canEditImage,
            isApplyingImageEdits = isApplyingImageEdits,
            displayImageEditError = displayImageEditError,
            sendActionState = sendActionState.value,
            textEditorState = textEditorState,
            mediaOptimizationSelectorState = mediaOptimizationSelectorState,
            displayFileTooLargeError = displayFileTooLargeError,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.preProcessAttachment(
        attachment: Attachment,
        mediaOptimizationConfig: MediaOptimizationConfig,
        displayProgress: Boolean,
        sendActionState: MutableState<SendActionState>,
    ) = launch(dispatchers.io) {
        when (attachment) {
            is Attachment.Media -> {
                preProcessMedia(
                    mediaAttachment = attachment,
                    mediaOptimizationConfig = mediaOptimizationConfig,
                    displayProgress = displayProgress,
                    sendActionState = sendActionState,
                )
            }
        }
    }

    private suspend fun preProcessMedia(
        mediaAttachment: Attachment.Media,
        mediaOptimizationConfig: MediaOptimizationConfig,
        displayProgress: Boolean,
        sendActionState: MutableState<SendActionState>,
    ) {
        sendActionState.value = SendActionState.Sending.Processing(displayProgress = displayProgress)
        mediaSender.preProcessMedia(
            uri = mediaAttachment.localMedia.uri,
            mimeType = mediaAttachment.localMedia.info.mimeType,
            mediaOptimizationConfig = mediaOptimizationConfig,
        ).fold(
            onSuccess = { mediaUploadInfo ->
                Timber.d("Media ${mediaUploadInfo.file.path.orEmpty().hash()} finished processing, it's now ready to upload")
                sendActionState.value = SendActionState.Sending.ReadyToUpload(mediaUploadInfo)
            },
            onFailure = {
                Timber.e(it, "Failed to pre-process media")
                if (it is CancellationException) {
                    throw it
                } else {
                    sendActionState.value = SendActionState.Failure(it, null)
                }
            }
        )
    }

    private fun dismiss(
        sendActionState: MutableState<SendActionState>,
        editedTempFile: File?,
    ) {
        // Delete the temporary file
        when (attachment) {
            is Attachment.Media -> {
                temporaryUriDeleter.delete(attachment.localMedia.uri)
                sendActionState.value.mediaUploadInfo()?.let { data ->
                    cleanUp(data)
                }
            }
        }
        editedTempFile?.safeDelete()
        // Reset the sendActionState to ensure that dialog is closed before the screen
        sendActionState.value = SendActionState.Done
        onDoneListener()
    }

    private fun cleanUp(
        mediaUploadInfo: MediaUploadInfo,
    ) {
        mediaUploadInfo.allFiles().forEach { file ->
            file.safeDelete()
        }
    }

    private fun resetPreparedMedia(sendActionState: MutableState<SendActionState>) {
        sendActionState.value.mediaUploadInfo()?.let(::cleanUp)
        mediaSender.cleanUp()
        sendActionState.value = SendActionState.Idle
    }

    private suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        sendActionState: MutableState<SendActionState>,
        dismissAfterSend: Boolean,
        inReplyToEventId: EventId?,
    ) = runCatchingExceptions {
        sendActionState.value = SendActionState.Sending.Uploading(mediaUploadInfo)
        mediaSender.sendPreProcessedMedia(
            mediaUploadInfo = mediaUploadInfo,
            caption = caption,
            formattedCaption = null,
            inReplyToEventId = inReplyToEventId,
        ).getOrThrow()
    }.fold(
        onSuccess = {
            cleanUp(mediaUploadInfo)
            // Reset the sendActionState to ensure that dialog is closed before the screen
            sendActionState.value = SendActionState.Done

            if (dismissAfterSend) {
                onDoneListener()
            }
        },
        onFailure = { error ->
            Timber.e(error, "Failed to send attachment")
            if (error is CancellationException) {
                throw error
            } else {
                sendActionState.value = SendActionState.Failure(error, mediaUploadInfo)
            }
        }
    )
}
