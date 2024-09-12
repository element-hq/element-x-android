/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
