/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.mediaupload.api.MediaSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.coroutineContext

class AttachmentsPreviewPresenter @AssistedInject constructor(
    @Assisted private val attachment: Attachment,
    private val mediaSender: MediaSender,
) : Presenter<AttachmentsPreviewState> {
    @AssistedFactory
    interface Factory {
        fun create(attachment: Attachment): AttachmentsPreviewPresenter
    }

    @Composable
    override fun present(): AttachmentsPreviewState {
        val coroutineScope = rememberCoroutineScope()

        val sendActionState = remember {
            mutableStateOf<SendActionState>(SendActionState.Idle)
        }

        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        fun handleEvents(attachmentsPreviewEvents: AttachmentsPreviewEvents) {
            when (attachmentsPreviewEvents) {
                AttachmentsPreviewEvents.SendAttachment -> ongoingSendAttachmentJob.value = coroutineScope.sendAttachment(attachment, sendActionState)
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
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.sendAttachment(
        attachment: Attachment,
        sendActionState: MutableState<SendActionState>,
    ) = launch {
        when (attachment) {
            is Attachment.Media -> {
                sendMedia(
                    mediaAttachment = attachment,
                    sendActionState = sendActionState,
                )
            }
        }
    }

    private suspend fun sendMedia(
        mediaAttachment: Attachment.Media,
        sendActionState: MutableState<SendActionState>,
    ) = runCatching {
        val context = coroutineContext
        val progressCallback = object : ProgressCallback {
            override fun onProgress(current: Long, total: Long) {
                if (context.isActive) {
                    sendActionState.value = SendActionState.Sending.Uploading(current.toFloat() / total.toFloat())
                }
            }
        }
        sendActionState.value = SendActionState.Sending.Processing
        mediaSender.sendMedia(
            uri = mediaAttachment.localMedia.uri,
            mimeType = mediaAttachment.localMedia.info.mimeType,
            compressIfPossible = mediaAttachment.compressIfPossible,
            progressCallback = progressCallback
        ).getOrThrow()
    }.fold(
        onSuccess = {
            sendActionState.value = SendActionState.Done
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
