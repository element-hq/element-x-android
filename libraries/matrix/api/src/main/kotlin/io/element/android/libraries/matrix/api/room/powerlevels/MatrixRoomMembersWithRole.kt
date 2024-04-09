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

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.joinedRoomMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Return a flow of the list of room members who are still in the room (with membership == RoomMembershipState.JOIN)
 * and who have the given role.
 */
fun MatrixRoom.usersWithRole(role: RoomMember.Role): Flow<ImmutableList<RoomMember>> {
    return roomInfoFlow
        .map { it.userPowerLevels.filter { (_, powerLevel) -> RoomMember.Role.forPowerLevel(powerLevel) == role } }
        .combine(membersStateFlow) { powerLevels, membersState ->
            membersState.joinedRoomMembers()
                .filter { powerLevels.containsKey(it.userId) }
                .toPersistentList()
        }
        .distinctUntilChanged()
}
