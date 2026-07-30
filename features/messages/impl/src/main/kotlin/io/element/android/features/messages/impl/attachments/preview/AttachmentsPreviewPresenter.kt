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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEditorState
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEdits
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorPresenter
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.features.messages.impl.attachments.video.VideoCompressionPresetSelector
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@AssistedInject
class AttachmentsPreviewPresenter(
    @Assisted private val attachments: ImmutableList<Attachment>,
    @Assisted private val onDoneListener: OnDoneListener,
    @Assisted private val timelineMode: Timeline.Mode,
    @Assisted private val inReplyToEventId: EventId?,
    mediaSenderFactory: MediaSenderFactory,
    private val permalinkBuilder: PermalinkBuilder,
    private val temporaryUriDeleter: TemporaryUriDeleter,
    private val attachmentImageEditor: AttachmentImageEditor,
    private val mediaOptimizationSelectorPresenterFactory: MediaOptimizationSelectorPresenter.Factory,
    private val videoCompressionPresetSelector: VideoCompressionPresetSelector,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
) : Presenter<AttachmentsPreviewState> {
    @AssistedFactory
    interface Factory {
        fun create(
            attachments: ImmutableList<Attachment>,
            timelineMode: Timeline.Mode,
            onDoneListener: OnDoneListener,
            inReplyToEventId: EventId?,
        ): AttachmentsPreviewPresenter
    }

    data class AttachmentAndEdits(
        val attachment: Attachment,
        val edits: AttachmentImageEdits,
    )

    private val mediaSender = mediaSenderFactory.create(timelineMode)

    @Composable
    override fun present(): AttachmentsPreviewState {
        val coroutineScope = rememberCoroutineScope()

        val sendActionState = remember {
            mutableStateOf<SendActionState>(SendActionState.Idle)
        }
        var canEditImage by remember { mutableStateOf(false) }
        var imageEditorState by remember { mutableStateOf<AttachmentImageEditorState?>(null) }
        var isApplyingImageEdits by remember { mutableStateOf(false) }
        var displayImageEditError by remember { mutableStateOf(false) }
        var editedTempFiles by remember { mutableStateOf<Map<Int, File>>(emptyMap()) }

        val markdownTextEditorState = rememberMarkdownTextEditorState(initialText = null, initialFocus = false)
        val textEditorState by rememberUpdatedState(
            TextEditorState.Markdown(markdownTextEditorState, isRoomEncrypted = null)
        )

        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        var currentIndex by remember { mutableIntStateOf(0) }

        var attachmentsAndEdits by remember {
            mutableStateOf(
                attachments.map {
                    AttachmentAndEdits(it, AttachmentImageEdits())
                }
            )
        }

        val editedAttachments by remember {
            derivedStateOf {
                attachmentsAndEdits.map { it.attachment }.toImmutableList()
            }
        }

        var preprocessMediaJob by remember { mutableStateOf<Job?>(null) }

        val mediaOptimizationSelectorPresenters = remember {
            attachments
                .filterIsInstance<Attachment.Media>()
                .mapIndexed { index, attachment ->
                    mediaOptimizationSelectorPresenterFactory.create(
                        index = index,
                        localMedia = attachment.localMedia,
                        sendAsFile = attachment.sendAsFile,
                    )
                }
        }
        val mediaOptimizationSelectorStates by rememberUpdatedState(
            mediaOptimizationSelectorPresenters.map {
                it.present()
            }.toImmutableList()
        )

        val observableSendState = snapshotFlow { sendActionState.value }

        var displayFileTooLargeError by remember { mutableStateOf(false) }

        LaunchedEffect(
            mediaOptimizationSelectorStates,
            imageEditorState,
            isApplyingImageEdits,
            editedAttachments,
        ) {
            if (mediaOptimizationSelectorStates.any { it.displayMediaSelectorViews == true } ||
                imageEditorState != null ||
                isApplyingImageEdits
            ) {
                // If any of the media optimization selectors are displayed, we don't want to pre-process the media yet
                return@LaunchedEffect
            }
            // If the media optimization selector is not displayed, we can pre-process the media
            // to prepare it for sending. This is done to avoid blocking the UI thread when the
            // user clicks on the send button.
            val configs = mediaOptimizationSelectorStates.mapIndexed { index, mediaOptimizationSelectorState ->
                getAutoPreprocessMediaOptimizationConfig(
                    mediaAttachment = editedAttachments[index] as Attachment.Media,
                    mediaOptimizationSelectorState = mediaOptimizationSelectorState,
                )
            }
            preprocessMediaJob?.cancel()
            preprocessMediaJob = coroutineScope.launch(dispatchers.io) {
                preProcessAttachments(
                    attachments = editedAttachments,
                    mediaOptimizationConfigs = configs,
                    displayProgress = false,
                    sendActionState = sendActionState,
                )
            }
        }

        LaunchedEffect(currentIndex) {
            val currentMedia = (attachments.getOrNull(currentIndex) as? Attachment.Media)?.localMedia
            if (currentMedia != null) {
                canEditImage = currentMedia.info.canEditImage() || attachmentImageEditor.canEdit(currentMedia)
            }
        }

        val maxUploadSize = mediaOptimizationSelectorStates.firstNotNullOfOrNull {
            it.maxUploadSize.dataOrNull()
        }
        LaunchedEffect(maxUploadSize) {
            if (maxUploadSize != null) {
                // If file size is not known, we're permissive and allow sending. The SDK will cancel the upload if needed.
                displayFileTooLargeError = attachments.any { attachment ->
                    when (attachment) {
                        is Attachment.Media -> {
                            val isImageFile = attachment.localMedia.info.isImageAttachment()
                            val isVideoFile = attachment.localMedia.info.mimeType.isMimeTypeVideo()
                            if (isImageFile || isVideoFile) {
                                false
                            } else {
                                val fileSize = attachment.localMedia.info.fileSize ?: 0L
                                maxUploadSize < fileSize
                            }
                        }
                    }
                }
            }
        }

        mediaOptimizationSelectorStates.forEach { mediaOptimizationSelectorState ->
            val videoSizeEstimations = mediaOptimizationSelectorState.videoSizeEstimations.dataOrNull()
            LaunchedEffect(videoSizeEstimations) {
                if (videoSizeEstimations != null) {
                    // Check if the video size estimations are too large for the max upload size
                    displayFileTooLargeError = videoSizeEstimations.none { it.canUpload }
                }
            }
        }

        fun handleEvent(event: AttachmentsPreviewEvent) {
            when (event) {
                is AttachmentsPreviewEvent.SendAttachment -> {
                    ongoingSendAttachmentJob.value = coroutineScope.launch {
                        if (preprocessMediaJob?.isActive != true && sendActionState.value !is SendActionState.Sending.ReadyToUpload) {
                            val configs = mediaOptimizationSelectorStates.map {
                                MediaOptimizationConfig(
                                    compressImages = it.isImageOptimizationEnabled
                                        ?: mediaOptimizationConfigProvider.get().compressImages,
                                    videoCompressionPreset = it.selectedVideoPreset
                                        ?: mediaOptimizationConfigProvider.get().videoCompressionPreset,
                                )
                            }
                            preprocessMediaJob = coroutineScope.launch(dispatchers.io) {
                                preProcessAttachments(
                                    attachments = editedAttachments,
                                    mediaOptimizationConfigs = configs,
                                    displayProgress = true,
                                    sendActionState = sendActionState,
                                )
                            }
                        }

                        // If the processing was hidden before, make it visible now
                        if (sendActionState.value is SendActionState.Sending.Processing) {
                            sendActionState.value = SendActionState.Sending.Processing(displayProgress = true)
                        }

                        // Wait until the media is ready to be uploaded
                        val allMediaUploadInfos = observableSendState.firstInstanceOf<SendActionState.Sending.ReadyToUpload>().mediaInfos

                        // Pre-processing is done, send the attachment
                        val caption = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
                            .takeIf { it.isNotEmpty() }

                        val editedTempFilesToDelete = editedTempFiles
                        editedTempFiles = emptyMap()

                        // Send the media using the session coroutine scope so it doesn't matter if this screen or the chat one are closed
                        sessionCoroutineScope.launch(dispatchers.io) {
                            sendMedia(
                                mediaUploadInfos = allMediaUploadInfos,
                                caption = caption,
                                sendActionState = sendActionState,
                                inReplyToEventId = inReplyToEventId,
                            )

                            // Clean up the pre-processed media after it's been sent
                            mediaSender.cleanUp()
                            editedTempFilesToDelete.values.forEach { it.safeDelete() }
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
                    dismiss(
                        attachments = editedAttachments,
                        sendActionState = sendActionState,
                        editedTempFiles = editedTempFiles,
                    )
                }
                AttachmentsPreviewEvent.CancelAndClearSendState -> {
                    // Cancel media sending
                    ongoingSendAttachmentJob.value?.let {
                        it.cancel()
                        ongoingSendAttachmentJob.value = null
                    }

                    val mediaUploadInfoList = sendActionState.value.mediaUploadInfoList()
                    sendActionState.value = if (mediaUploadInfoList != null) {
                        SendActionState.Sending.ReadyToUpload(mediaUploadInfoList)
                    } else {
                        SendActionState.Idle
                    }
                }
                AttachmentsPreviewEvent.OpenImageEditor -> {
                    val currentLocalMedia = (attachments.getOrNull(currentIndex) as? Attachment.Media)?.localMedia ?: return
                    val resolvedCanEditImage = canEditImage || currentLocalMedia.info.canEditImage()
                    if (resolvedCanEditImage) {
                        preprocessMediaJob?.cancel()
                        preprocessMediaJob = null
                        resetPreparedMedia(sendActionState)
                        imageEditorState = AttachmentImageEditorState(
                            localMedia = currentLocalMedia,
                            edits = attachmentsAndEdits.get(currentIndex).edits,
                            previewDebug = false,
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
                AttachmentsPreviewEvent.RotateImageToTheLeft -> {
                    val pendingState = imageEditorState ?: return
                    imageEditorState = pendingState.copy(
                        edits = pendingState.edits.rotateAntiClockwise()
                    )
                }
                AttachmentsPreviewEvent.FlipImageHorizontally -> {
                    val pendingState = imageEditorState ?: return
                    imageEditorState = pendingState.copy(
                        edits = pendingState.edits.flipHorizontally()
                    )
                }
                AttachmentsPreviewEvent.FlipImageVertically -> {
                    val pendingState = imageEditorState ?: return
                    imageEditorState = pendingState.copy(
                        edits = pendingState.edits.flipVertically()
                    )
                }
                AttachmentsPreviewEvent.ResetImageEdits -> {
                    imageEditorState = imageEditorState?.copy(
                        edits = AttachmentImageEdits()
                    )
                }
                AttachmentsPreviewEvent.ApplyImageEdits -> {
                    val pendingState = imageEditorState ?: return
                    if (!pendingState.edits.hasChanges) {
                        editedTempFiles[currentIndex]?.safeDelete()
                        editedTempFiles = editedTempFiles - currentIndex
                        val currentAttachment = attachmentsAndEdits[currentIndex].attachment
                        attachmentsAndEdits = attachmentsAndEdits.toMutableList().also {
                            it[currentIndex] = AttachmentAndEdits(
                                currentAttachment,
                                pendingState.edits,
                            )
                        }.toImmutableList()
                        imageEditorState = null
                        resetPreparedMedia(sendActionState)
                        return
                    }
                    isApplyingImageEdits = true
                    displayImageEditError = false
                    coroutineScope.launch {
                        val result = withContext(dispatchers.io) {
                            attachmentImageEditor.exportEdits(
                                localMedia = pendingState.localMedia,
                                edits = pendingState.edits,
                            )
                        }
                        result.fold(
                            onSuccess = { editedMedia ->
                                editedTempFiles[currentIndex]?.safeDelete()
                                editedTempFiles = editedTempFiles + (currentIndex to editedMedia.file)
                                val currentAttachment = Attachment.Media(editedMedia.localMedia)
                                attachmentsAndEdits = attachmentsAndEdits.toMutableList().also {
                                    it[currentIndex] = AttachmentAndEdits(
                                        currentAttachment,
                                        pendingState.edits,
                                    )
                                }.toImmutableList()
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
                is AttachmentsPreviewEvent.SetCurrentCarouselIndex -> {
                    currentIndex = event.index
                }
            }
        }

        return AttachmentsPreviewState(
            attachments = editedAttachments,
            imageEditorState = imageEditorState,
            canEditImage = canEditImage,
            isApplyingImageEdits = isApplyingImageEdits,
            displayImageEditError = displayImageEditError,
            sendActionState = sendActionState.value,
            textEditorState = textEditorState,
            mediaOptimizationSelectorState = mediaOptimizationSelectorStates[currentIndex],
            displayFileTooLargeError = displayFileTooLargeError,
            currentIndex = currentIndex,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun getAutoPreprocessMediaOptimizationConfig(
        mediaAttachment: Attachment.Media,
        mediaOptimizationSelectorState: MediaOptimizationSelectorState,
    ): MediaOptimizationConfig {
        return if (mediaAttachment.sendAsFile) {
            // If we're sending the media as a file, we can skip image compression and we should select the highest video compression preset that still fits
            // the upload limit (if the estimations are available)
            val videoCompressionPreset = videoCompressionPresetSelector.selectBestVideoPreset(
                expectedVideoPreset = VideoCompressionPreset.HIGH,
                videoSizeEstimations = mediaOptimizationSelectorState.videoSizeEstimations,
            ).dataOrNull() ?: VideoCompressionPreset.HIGH

            MediaOptimizationConfig(
                compressImages = false,
                videoCompressionPreset = videoCompressionPreset,
            )
        } else {
            MediaOptimizationConfig(
                compressImages = mediaOptimizationSelectorState.isImageOptimizationEnabled
                    ?: mediaOptimizationConfigProvider.get().compressImages,
                videoCompressionPreset = mediaOptimizationSelectorState.selectedVideoPreset
                    ?: mediaOptimizationConfigProvider.get().videoCompressionPreset,
            )
        }
    }

    private suspend fun preProcessAttachments(
        attachments: List<Attachment>,
        mediaOptimizationConfigs: List<MediaOptimizationConfig>,
        displayProgress: Boolean,
        sendActionState: MutableState<SendActionState>,
    ) {
        sendActionState.value = SendActionState.Sending.Processing(displayProgress = displayProgress)
        val mediaUploadInfos = mutableListOf<MediaUploadInfo>()
        attachments.forEachIndexed { index, attachment ->
            when (attachment) {
                is Attachment.Media -> {
                    mediaSender.preProcessMedia(
                        uri = attachment.localMedia.uri,
                        mimeType = attachment.localMedia.info.mimeType,
                        mediaOptimizationConfig = mediaOptimizationConfigs[index],
                    ).fold(
                        onSuccess = { mediaUploadInfo ->
                            Timber.d("Media ${mediaUploadInfo.file.path.orEmpty().hash()} finished processing")
                            mediaUploadInfos.add(mediaUploadInfo)
                        },
                        onFailure = {
                            Timber.e(it, "Failed to pre-process media")
                            if (it is CancellationException) {
                                throw it
                            } else {
                                sendActionState.value = SendActionState.Failure(it, emptyList())
                                return
                            }
                        }
                    )
                }
            }
        }
        sendActionState.value = SendActionState.Sending.ReadyToUpload(mediaUploadInfos)
    }

    private fun dismiss(
        attachments: List<Attachment>,
        sendActionState: MutableState<SendActionState>,
        editedTempFiles: Map<Int, File> = emptyMap(),
    ) {
        for (attachment in attachments) {
            when (attachment) {
                is Attachment.Media -> {
                    temporaryUriDeleter.delete(attachment.localMedia.uri)
                }
            }
        }
        val uploadInfos = (sendActionState.value as? SendActionState.Sending.ReadyToUpload)?.mediaInfos
        uploadInfos?.forEach { cleanUp(it) }
        editedTempFiles.values.forEach { it.safeDelete() }
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
        sendActionState.value.mediaUploadInfoList()?.forEach(::cleanUp)
        mediaSender.cleanUp()
        sendActionState.value = SendActionState.Idle
    }

    private suspend fun sendMedia(
        mediaUploadInfos: List<MediaUploadInfo>,
        caption: String?,
        sendActionState: MutableState<SendActionState>,
        inReplyToEventId: EventId?,
    ) = runCatchingExceptions {
        if (mediaUploadInfos.size == 1) {
            sendActionState.value = SendActionState.Sending.Uploading(mediaUploadInfos)
            mediaSender.sendPreProcessedMedia(
                mediaUploadInfo = mediaUploadInfos.first(),
                caption = caption,
                formattedCaption = null,
                inReplyToEventId = inReplyToEventId,
            ).getOrThrow()
        } else {
            mediaSender.sendGallery(
                mediaUploadInfos = mediaUploadInfos,
                caption = caption,
                formattedCaption = null,
                inReplyToEventId = inReplyToEventId,
            ).getOrThrow()
        }
    }.fold(
        onSuccess = {
            mediaUploadInfos.forEach { cleanUp(it) }
            sendActionState.value = SendActionState.Done
            onDoneListener()
        },
        onFailure = { error ->
            Timber.e(error, "Failed to send attachment")
            if (error is CancellationException) {
                throw error
            } else {
                sendActionState.value = SendActionState.Failure(error, mediaUploadInfos)
            }
        }
    )
}
