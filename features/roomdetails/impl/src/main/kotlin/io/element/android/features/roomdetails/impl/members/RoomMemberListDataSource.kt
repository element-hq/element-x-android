/*
 * Copyright (c) 2023 New Vector Ltd
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
