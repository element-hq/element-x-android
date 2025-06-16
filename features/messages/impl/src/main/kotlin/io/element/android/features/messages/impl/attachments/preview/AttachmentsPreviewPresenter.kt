/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.firstInstanceOf
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.api.allFiles
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.rememberMarkdownTextEditorState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.coroutineContext

class AttachmentsPreviewPresenter @AssistedInject constructor(
    @Assisted private val attachment: Attachment,
    @Assisted private val onDoneListener: OnDoneListener,
    private val mediaSender: MediaSender,
    private val permalinkBuilder: PermalinkBuilder,
    private val temporaryUriDeleter: TemporaryUriDeleter,
    private val featureFlagService: FeatureFlagService,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<AttachmentsPreviewState> {
    @AssistedFactory
    interface Factory {
        fun create(
            attachment: Attachment,
            onDoneListener: OnDoneListener,
        ): AttachmentsPreviewPresenter
    }

    @Composable
    override fun present(): AttachmentsPreviewState {
        val coroutineScope = rememberCoroutineScope()

        val sendActionState = remember {
            mutableStateOf<SendActionState>(SendActionState.Idle)
        }

        val markdownTextEditorState = rememberMarkdownTextEditorState(initialText = null, initialFocus = false)
        val textEditorState by rememberUpdatedState(
            TextEditorState.Markdown(markdownTextEditorState, isRoomEncrypted = null)
        )

        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        val allowCaption by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MediaCaptionCreation)
        }.collectAsState(initial = false)
        val showCaptionCompatibilityWarning by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MediaCaptionWarning)
        }.collectAsState(initial = false)

        var useSendQueue by remember { mutableStateOf(false) }
        var preprocessMediaJob by remember { mutableStateOf<Job?>(null) }
        LaunchedEffect(Unit) {
            useSendQueue = featureFlagService.isFeatureEnabled(FeatureFlags.MediaUploadOnSendQueue)

            preprocessMediaJob = preProcessAttachment(
                attachment,
                sendActionState
            )
        }

        val observableSendState = snapshotFlow { sendActionState.value }

        fun handleEvents(attachmentsPreviewEvents: AttachmentsPreviewEvents) {
            when (attachmentsPreviewEvents) {
                is AttachmentsPreviewEvents.SendAttachment -> {
                    ongoingSendAttachmentJob.value = coroutineScope.launch {
                        // If the processing was hidden before, make it visible now
                        if (sendActionState.value is SendActionState.Sending.Processing) {
                            sendActionState.value = SendActionState.Sending.Processing(displayProgress = true)
                        }

                        // Wait until the media is ready to be uploaded
                        val mediaUploadInfo = observableSendState.firstInstanceOf<SendActionState.Sending.ReadyToUpload>().mediaInfo

                        // Pre-processing is done, send the attachment
                        val caption = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
                            .takeIf { it.isNotEmpty() }

                        // If we're supposed to send the media as a background job, we can dismiss this screen already
                        if (useSendQueue && coroutineContext.isActive) {
                            onDoneListener()
                        }

                        // If using the send queue, send it using the session coroutine scope so it doesn't matter if this screen or the chat one are closed
                        val sendMediaCoroutineScope = if (useSendQueue) sessionCoroutineScope else coroutineScope
                        sendMediaCoroutineScope.launch(dispatchers.io) {
                            sendPreProcessedMedia(
                                mediaUploadInfo = mediaUploadInfo,
                                caption = caption,
                                sendActionState = sendActionState,
                                dismissAfterSend = !useSendQueue,
                                replyParameters = null,
                            )
                        }
                    }
                }
                AttachmentsPreviewEvents.CancelAndDismiss -> {
                    // Cancel media preprocessing and sending
                    preprocessMediaJob?.cancel()
                    ongoingSendAttachmentJob.value?.cancel()

                    // Dismiss the screen
                    dismiss(
                        attachment,
                        sendActionState,
                    )
                }
                AttachmentsPreviewEvents.CancelAndClearSendState -> {
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
            }
        }

        return AttachmentsPreviewState(
            attachment = attachment,
            sendActionState = sendActionState.value,
            textEditorState = textEditorState,
            allowCaption = allowCaption,
            showCaptionCompatibilityWarning = showCaptionCompatibilityWarning,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.preProcessAttachment(
        attachment: Attachment,
        sendActionState: MutableState<SendActionState>,
    ) = launch(dispatchers.io) {
        when (attachment) {
            is Attachment.Media -> {
                preProcessMedia(
                    mediaAttachment = attachment,
                    sendActionState = sendActionState,
                )
            }
        }
    }

    private suspend fun preProcessMedia(
        mediaAttachment: Attachment.Media,
        sendActionState: MutableState<SendActionState>,
    ) {
        sendActionState.value = SendActionState.Sending.Processing(displayProgress = false)
        mediaSender.preProcessMedia(
            uri = mediaAttachment.localMedia.uri,
            mimeType = mediaAttachment.localMedia.info.mimeType,
        ).fold(
            onSuccess = { mediaUploadInfo ->
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
        attachment: Attachment,
        sendActionState: MutableState<SendActionState>,
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

    private suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        sendActionState: MutableState<SendActionState>,
        dismissAfterSend: Boolean,
        replyParameters: ReplyParameters?,
    ) = runCatchingExceptions {
        val context = coroutineContext
        val progressCallback = object : ProgressCallback {
            override fun onProgress(current: Long, total: Long) {
                // Note will not happen if useSendQueue is true
                if (context.isActive) {
                    sendActionState.value = SendActionState.Sending.Uploading(current.toFloat() / total.toFloat(), mediaUploadInfo)
                }
            }
        }
        mediaSender.sendPreProcessedMedia(
            mediaUploadInfo = mediaUploadInfo,
            caption = caption,
            formattedCaption = null,
            progressCallback = progressCallback,
            replyParameters = replyParameters,
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
