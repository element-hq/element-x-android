/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import javax.inject.Inject

interface DeclineInvite {
    suspend operator fun invoke(roomId: RoomId, blockUser: Boolean, reportRoom: Boolean, reportReason: String?)

    sealed class Exception : kotlin.Exception() {
        data object RoomNotFound : Exception()
        data object DeclineInviteFailed : Exception()
        data object ReportRoomFailed : Exception()
        data object BlockUserFailed : Exception()
    }
}

@ContributesBinding(SessionScope::class)
class DefaultDeclineInvite @Inject constructor(
    private val client: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val seenInvitesStore: SeenInvitesStore,
) : DeclineInvite {

    override suspend fun invoke(
        roomId: RoomId,
        blockUser: Boolean,
        reportRoom: Boolean,
        reportReason: String?
    ) {
        val room = client.getRoom(roomId) ?: throw DeclineInvite.Exception.RoomNotFound
        room.use {
            room.leave()
                .onFailure { throw DeclineInvite.Exception.DeclineInviteFailed }
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
                        .onFailure { throw DeclineInvite.Exception.BlockUserFailed }
                }
            }
            if (reportRoom) {
                room
                    .reportRoom(reportReason)
                    .onFailure { throw DeclineInvite.Exception.ReportRoomFailed }
            }
        }
    }
}
