/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.share.impl

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharePresenter @AssistedInject constructor(
    @Assisted private val intent: Intent,
    private val appCoroutineScope: CoroutineScope,
    private val shareIntentHandler: ShareIntentHandler,
    private val matrixClient: MatrixClient,
    private val mediaPreProcessor: MediaPreProcessor,
) : Presenter<ShareState> {
    @AssistedFactory
    interface Factory {
        fun create(intent: Intent): SharePresenter
    }

    private val shareActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    fun onRoomSelected(roomIds: List<RoomId>) {
        appCoroutineScope.share(intent, roomIds)
    }

    @Composable
    override fun present(): ShareState {
        fun handleEvents(event: ShareEvents) {
            when (event) {
                ShareEvents.ClearError -> shareActionState.value = AsyncAction.Uninitialized
            }
        }

        return ShareState(
            shareAction = shareActionState.value,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.share(
        intent: Intent,
        roomIds: List<RoomId>,
    ) = launch {
        suspend {
            val result = shareIntentHandler.handleIncomingShareIntent(
                intent,
                onUris = { filesToShare ->
                    if (filesToShare.isEmpty()) {
                        false
                    } else {
                        roomIds
                            .map { roomId ->
                                val room = matrixClient.getRoom(roomId) ?: return@map false
                                val mediaSender = MediaSender(preProcessor = mediaPreProcessor, room = room)
                                filesToShare
                                    .map { fileToShare ->
                                        mediaSender.sendMedia(
                                            uri = fileToShare.uri,
                                            mimeType = fileToShare.mimeType,
                                            compressIfPossible = true,
                                        ).isSuccess
                                    }
                                    .all { it }
                            }
                            .all { it }
                    }
                },
                onPlainText = { text ->
                    roomIds
                        .map { roomId ->
                            matrixClient.getRoom(roomId)?.sendMessage(
                                body = text,
                                htmlBody = null,
                                mentions = emptyList(),
                            )?.isSuccess.orFalse()
                        }
                        .all { it }
                }
            )
            if (!result) {
                error("Failed to handle incoming share intent")
            }
            roomIds
        }.runCatchingUpdatingState(shareActionState)
    }
}
