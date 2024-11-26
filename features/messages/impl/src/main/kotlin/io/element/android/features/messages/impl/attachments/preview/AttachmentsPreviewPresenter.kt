/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
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
    private val featureFlagsService: FeatureFlagService,
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
            TextEditorState.Markdown(markdownTextEditorState)
        )

        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        val userSentAttachment = remember { mutableStateOf(false) }

        val mediaUploadInfoState = remember { mutableStateOf<AsyncData<MediaUploadInfo>>(AsyncData.Uninitialized) }
        var prePropressingJob: Job? = null
        LaunchedEffect(Unit) {
            prePropressingJob = preProcessAttachment(
                attachment,
                mediaUploadInfoState,
            )
        }

        LaunchedEffect(userSentAttachment.value, mediaUploadInfoState.value) {
            val mediaUploadInfo = mediaUploadInfoState.value
            if (userSentAttachment.value && mediaUploadInfo.isReady())
                if (mediaUploadInfo is AsyncData.Success) {
                    val caption = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
                        .takeIf { it.isNotEmpty() }
                    ongoingSendAttachmentJob.value = coroutineScope.launch {
                        sendPreProcessedMedia(
                            mediaUploadInfo = mediaUploadInfo.data,
                            caption = caption,
                            sendActionState = sendActionState,
                        )
                    }
                } else if (mediaUploadInfo is AsyncData.Failure) {
                    sendActionState.value = SendActionState.Failure(mediaUploadInfo.error)
                }
            // else: cannot happen since we filtered with isReady()
        }

        fun handleEvents(attachmentsPreviewEvents: AttachmentsPreviewEvents) {
            when (attachmentsPreviewEvents) {
                is AttachmentsPreviewEvents.SendAttachment -> coroutineScope.launch {
                    val useSendQueue = featureFlagsService.isFeatureEnabled(FeatureFlags.MediaUploadOnSendQueue)
                    userSentAttachment.value = true
                    val instantSending = mediaUploadInfoState.value.isReady() && useSendQueue
                    sendActionState.value = if (instantSending) {
                        SendActionState.Sending.InstantSending
                    } else {
                        SendActionState.Sending.Processing
                    }
                }
                AttachmentsPreviewEvents.Cancel -> {
                    coroutineScope.cancel(
                        attachment,
                        prePropressingJob,
                        mediaUploadInfoState.value,
                        sendActionState,
                    )
                }
                AttachmentsPreviewEvents.ClearSendState -> {
                    ongoingSendAttachmentJob.value?.let {
                        it.cancel()
                        ongoingSendAttachmentJob.value = null
                    }
                    sendActionState.value = SendActionState.Idle
                }
            }
        }

        return AttachmentsPreviewState(
            attachment = attachment,
            sendActionState = sendActionState.value,
            textEditorState = textEditorState,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.preProcessAttachment(
        attachment: Attachment,
        mediaUploadInfoState: MutableState<AsyncData<MediaUploadInfo>>,
    ) = launch {
        when (attachment) {
            is Attachment.Media -> {
                preProcessMedia(
                    mediaAttachment = attachment,
                    mediaUploadInfoState = mediaUploadInfoState,
                )
            }
        }
    }

    private suspend fun preProcessMedia(
        mediaAttachment: Attachment.Media,
        mediaUploadInfoState: MutableState<AsyncData<MediaUploadInfo>>,
    ) {
        mediaUploadInfoState.value = AsyncData.Loading()
        mediaSender.preProcessMedia(
            uri = mediaAttachment.localMedia.uri,
            mimeType = mediaAttachment.localMedia.info.mimeType,
        ).fold(
            onSuccess = { mediaUploadInfo ->
                mediaUploadInfoState.value = AsyncData.Success(mediaUploadInfo)
            },
            onFailure = {
                Timber.e(it, "Failed to pre-process media")
                if (it is CancellationException) {
                    throw it
                } else {
                    mediaUploadInfoState.value = AsyncData.Failure(it)
                }
            }
        )
    }

    private fun CoroutineScope.cancel(
        attachment: Attachment,
        preProcessingJob: Job?,
        mediaUploadInfo: AsyncData<MediaUploadInfo>,
        sendActionState: MutableState<SendActionState>,
    ) = launch {
        // Delete the temporary file
        when (attachment) {
            is Attachment.Media -> {
                temporaryUriDeleter.delete(attachment.localMedia.uri)
                preProcessingJob?.cancel()
                mediaUploadInfo.dataOrNull()?.let { data ->
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
    ) = runCatching {
        val context = coroutineContext
        val progressCallback = object : ProgressCallback {
            override fun onProgress(current: Long, total: Long) {
                // Note will not happen if useSendQueue is true
                if (context.isActive) {
                    sendActionState.value = SendActionState.Sending.Uploading(current.toFloat() / total.toFloat())
                }
            }
        }
        mediaSender.sendPreProcessedMedia(
            mediaUploadInfo = mediaUploadInfo,
            caption = caption,
            formattedCaption = null,
            progressCallback = progressCallback
        ).getOrThrow()
    }.fold(
        onSuccess = {
            cleanUp(mediaUploadInfo)
            // Reset the sendActionState to ensure that dialog is closed before the screen
            sendActionState.value = SendActionState.Done
            onDoneListener()
        },
        onFailure = { error ->
            Timber.e(error, "Failed to send attachment")
            if (error is CancellationException) {
                throw error
            } else {
                sendActionState.value = SendActionState.Failure(error)
            }
        }
    )
}
