/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.notifications.NotificationCleaner

interface DeclineInvite {
    suspend operator fun invoke(
        roomId: RoomId,
        blockUser: Boolean,
        reportRoom: Boolean,
        reportReason: String?
    ): Result<RoomId>

    sealed class Exception : kotlin.Exception() {
        data object RoomNotFound : Exception()
        data object DeclineInviteFailed : Exception()
        data object ReportRoomFailed : Exception()
        data object BlockUserFailed : Exception()
    }
}

@ContributesBinding(SessionScope::class)
class DefaultDeclineInvite(
    private val client: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val seenInvitesStore: SeenInvitesStore,
) : DeclineInvite {
    override suspend fun invoke(
        roomId: RoomId,
        blockUser: Boolean,
        reportRoom: Boolean,
        reportReason: String?
    ): Result<RoomId> {
        val room = client.getRoom(roomId) ?: return Result.failure(DeclineInvite.Exception.RoomNotFound)
        room.use {
            room.leave()
                .onFailure { return Result.failure(DeclineInvite.Exception.DeclineInviteFailed) }
                .onSuccess {
                    notificationCleaner.clearMembershipNotificationForRoom(
                        sessionId = client.sessionId,
                        roomId = roomId
                    )
                    seenInvitesStore.markAsUnSeen(roomId)
                }

            if (blockUser) {
                val userIdToBlock = room.info().inviter?.userId
                if (userIdToBlock != null) {
                    client
                        .ignoreUser(userIdToBlock)
                        .onFailure { return Result.failure(DeclineInvite.Exception.BlockUserFailed) }
                }
            }
            if (reportRoom) {
                room
                    .reportRoom(reportReason)
                    .onFailure { return Result.failure(DeclineInvite.Exception.ReportRoomFailed) }
            }
        }
        return Result.success(roomId)
    }
}
