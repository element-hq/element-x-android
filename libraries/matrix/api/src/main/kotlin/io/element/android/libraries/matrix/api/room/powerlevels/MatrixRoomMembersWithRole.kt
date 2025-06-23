/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.activeRoomMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Return a flow of the list of active room members who have the given role.
 */
fun BaseRoom.usersWithRole(role: RoomMember.Role): Flow<ImmutableList<RoomMember>> {
    return roomInfoFlow
        .map { it.userPowerLevels.filter { (_, powerLevel) -> RoomMember.Role.forPowerLevel(powerLevel) == role } }
        .combine(membersStateFlow) { powerLevels, membersState ->
            membersState.activeRoomMembers()
                .filter { powerLevels.containsKey(it.userId) }
                .toPersistentList()
        }
        .distinctUntilChanged()
}
