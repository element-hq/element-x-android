/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.recent

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.flow.first

private const val MAX_RECENT_DIRECT_ROOMS_TO_RETURN = 5

data class RecentDirectRoom(
    val roomId: RoomId,
    val matrixUser: MatrixUser,
)

suspend fun MatrixClient.getRecentDirectRooms(
    maxNumberOfResults: Int = MAX_RECENT_DIRECT_ROOMS_TO_RETURN,
): List<RecentDirectRoom> {
    val result = mutableListOf<RecentDirectRoom>()
    val foundUserIds = mutableSetOf<UserId>()
    getRecentlyVisitedRooms().getOrNull()?.let { roomIds ->
        roomIds
            .mapNotNull { roomId -> getRoom(roomId) }
            .filter { it.isDm() && it.isJoined() }
            .map { room ->
                val otherUser = room.getMembers().getOrNull()
                    ?.firstOrNull { it.userId != sessionId }
                    ?.takeIf { foundUserIds.add(it.userId) }
                    ?.toMatrixUser()
                if (otherUser != null) {
                    result.add(
                        RecentDirectRoom(room.roomId, otherUser)
                    )
                    // Return early to avoid useless computation
                    if (result.size >= maxNumberOfResults) {
                        return@map
                    }
                }
            }
    }
    return result
}

suspend fun BaseRoom.isJoined(): Boolean {
    return roomInfoFlow.first().currentUserMembership == CurrentUserMembership.JOINED
}
