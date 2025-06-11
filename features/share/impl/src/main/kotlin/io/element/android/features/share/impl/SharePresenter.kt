/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SharePresenter @AssistedInject constructor(
    @Assisted private val intent: Intent,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val shareIntentHandler: ShareIntentHandler,
    private val matrixClient: MatrixClient,
    private val mediaPreProcessor: MediaPreProcessor,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val activeRoomsHolder: ActiveRoomsHolder,
) : Presenter<ShareState> {
    @AssistedFactory
    interface Factory {
        fun create(intent: Intent): SharePresenter
    }

    private val shareActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    fun onRoomSelected(roomIds: List<RoomId>) {
        sessionCoroutineScope.share(intent, roomIds)
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

    private suspend fun getJoinedRoom(roomId: RoomId): JoinedRoom? {
        return activeRoomsHolder.getActiveRoom(matrixClient.sessionId)
            ?.takeIf { it.roomId == roomId }
            ?: matrixClient.getJoinedRoom(roomId)
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
                                val room = getJoinedRoom(roomId) ?: return@map false
                                val mediaSender = MediaSender(
                                    preProcessor = mediaPreProcessor,
                                    room = room,
                                    sessionPreferencesStore = sessionPreferencesStore,
                                )
                                filesToShare
                                    .map { fileToShare ->
                                        val result = mediaSender.sendMedia(
                                            uri = fileToShare.uri,
                                            mimeType = fileToShare.mimeType,
                                        )
                                        // If the coroutine was cancelled, destroy the room and rethrow the exception
                                        val cancellationException = result.exceptionOrNull() as? CancellationException
                                        if (cancellationException != null) {
                                            if (activeRoomsHolder.getActiveRoomMatching(matrixClient.sessionId, roomId) == null) {
                                                room.destroy()
                                            }
                                            throw cancellationException
                                        }
                                        result.isSuccess
                                    }
                                    .all { isSuccess -> isSuccess }
                                    .also {
                                        if (activeRoomsHolder.getActiveRoomMatching(matrixClient.sessionId, roomId) == null) {
                                            room.destroy()
                                        }
                                    }
                            }
                            .all { it }
                    }
                },
                onPlainText = { text ->
                    roomIds
                        .map { roomId ->
                            getJoinedRoom(roomId)?.liveTimeline?.sendMessage(
                                body = text,
                                htmlBody = null,
                                intentionalMentions = emptyList(),
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
