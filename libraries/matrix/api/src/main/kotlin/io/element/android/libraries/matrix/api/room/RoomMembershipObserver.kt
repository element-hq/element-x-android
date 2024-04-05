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

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomMembershipObserver {
    data class RoomMembershipUpdate(
        val roomId: RoomId,
        val isUserInRoom: Boolean,
        val change: MembershipChange,
    )

    private val _updates = MutableSharedFlow<RoomMembershipUpdate>(extraBufferCapacity = 10)
    val updates = _updates.asSharedFlow()

    suspend fun notifyUserLeftRoom(roomId: RoomId) {
        _updates.emit(RoomMembershipUpdate(roomId, false, MembershipChange.LEFT))
    }
}
