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

package io.element.android.libraries.matrix.api.room.recent

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoom
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
            .filter { it.isDm && it.isJoined() }
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

suspend fun MatrixRoom.isJoined(): Boolean {
    return roomInfoFlow.first().currentUserMembership == CurrentUserMembership.JOINED
}
