/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.share.api.OnSharedData
import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import io.element.android.libraries.mediaupload.api.MediaSenderRoomFactory
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import kotlin.coroutines.cancellation.CancellationException

@ContributesBinding(AppScope::class)
class DefaultShareDataSender(
    private val mediaSenderRoomFactory: MediaSenderRoomFactory,
    private val mediaOptimizationConfigProviderFactory: MediaOptimizationConfigProviderFactory,
    private val activeRoomsHolder: ActiveRoomsHolder,
    private val onSharedData: OnSharedData,
) : ShareDataSender {
    override suspend fun send(
        client: MatrixClient,
        shareIntentData: ShareIntentData,
        roomIds: List<RoomId>,
    ): Result<Unit> = runCatchingExceptions {
        val result = when (shareIntentData) {
            is ShareIntentData.PlainText -> {
                roomIds
                    .map { roomId ->
                        client.findJoinedRoom(roomId)?.liveTimeline?.sendMessage(
                            body = shareIntentData.content,
                            htmlBody = null,
                            intentionalMentions = emptyList(),
                        )?.isSuccess.orFalse()
                    }
                    .all { it }
            }
            is ShareIntentData.Uris -> {
                val filesToShare = shareIntentData.uris
                if (filesToShare.isEmpty()) {
                    false
                } else {
                    val mediaOptimizationConfigProvider = mediaOptimizationConfigProviderFactory.create(client)
                    roomIds
                        .map { roomId ->
                            val room = client.findJoinedRoom(roomId) ?: return@map false
                            val mediaSender = mediaSenderRoomFactory.create(client = client, room = room)
                            filesToShare
                                .map { fileToShare ->
                                    val result = mediaSender.sendMedia(
                                        caption = shareIntentData.text,
                                        uri = fileToShare.uri,
                                        mimeType = fileToShare.mimeType,
                                        mediaOptimizationConfig = mediaOptimizationConfigProvider.get(),
                                    )
                                    // If the coroutine was cancelled, destroy the room and rethrow the exception
                                    val cancellationException = result.exceptionOrNull() as? CancellationException
                                    if (cancellationException != null) {
                                        if (activeRoomsHolder.getActiveRoomMatching(client.sessionId, roomId) == null) {
                                            room.destroy()
                                        }
                                        throw cancellationException
                                    }
                                    result.isSuccess
                                }
                                .all { isSuccess -> isSuccess }
                                .also {
                                    if (activeRoomsHolder.getActiveRoomMatching(client.sessionId, roomId) == null) {
                                        room.destroy()
                                    }
                                }
                        }
                        .all { it }
                }
            }
        }

        // Handle post-processing of shared data
        onSharedData(shareIntentData)

        if (!result) {
            error("Failed to handle incoming share intent")
        }
    }

    private suspend fun MatrixClient.findJoinedRoom(roomId: RoomId): JoinedRoom? {
        return activeRoomsHolder.getActiveRoom(sessionId)
            ?.takeIf { it.roomId == roomId }
            ?: getJoinedRoom(roomId)
    }
}
