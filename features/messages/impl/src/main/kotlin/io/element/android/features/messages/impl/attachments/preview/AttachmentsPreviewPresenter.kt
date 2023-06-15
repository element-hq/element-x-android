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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.mediaupload.api.MediaSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
            mutableStateOf<Async<Unit>>(Async.Uninitialized)
        }

        fun handleEvents(attachmentsPreviewEvents: AttachmentsPreviewEvents) {
            when (attachmentsPreviewEvents) {
                AttachmentsPreviewEvents.SendAttachment -> coroutineScope.sendAttachment(attachment, sendActionState)
                AttachmentsPreviewEvents.ClearSendState -> sendActionState.value = Async.Uninitialized
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
        sendActionState: MutableState<Async<Unit>>,
    ) = launch {
        when (attachment) {
            is Attachment.Media -> {
                sendMedia(
                    mediaAttachment = attachment,
                    sendActionState = sendActionState
                )
            }
        }
    }

    private suspend fun sendMedia(
        mediaAttachment: Attachment.Media,
        sendActionState: MutableState<Async<Unit>>,
    ) {
        sendActionState.runUpdatingState {
            mediaSender.sendMedia(mediaAttachment.localMedia.uri, mediaAttachment.localMedia.info.mimeType, mediaAttachment.compressIfPossible)
        }
    }
}
