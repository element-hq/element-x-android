/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomMemberListDataSource @Inject constructor(
    private val room: MatrixRoom,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    suspend fun search(query: String): List<RoomMember> = withContext(coroutineDispatchers.io) {
        val roomMembersState = room.membersStateFlow.value
        val activeRoomMembers = roomMembersState.roomMembers()
            ?.filter { it.membership.isActive() }
            .orEmpty()
        val filteredMembers = if (query.isBlank()) {
            activeRoomMembers
        } else {
            activeRoomMembers.filter { member ->
                member.userId.value.contains(query, ignoreCase = true) ||
                    member.displayName?.contains(query, ignoreCase = true).orFalse()
            }
        }
        filteredMembers
    }
}
