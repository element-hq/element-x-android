/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomMembershipObserver {
    data class RoomMembershipUpdate(
        val roomId: RoomId,
        val isSpace: Boolean,
        val isUserInRoom: Boolean,
        val change: MembershipChange,
    )

    private val _updates = MutableSharedFlow<RoomMembershipUpdate>(extraBufferCapacity = 10)
    val updates = _updates.asSharedFlow()

    suspend fun notifyUserLeftRoom(
        roomId: RoomId,
        isSpace: Boolean,
        membershipBeforeLeft: CurrentUserMembership,
    ) {
        val membershipChange = when (membershipBeforeLeft) {
            CurrentUserMembership.INVITED -> MembershipChange.INVITATION_REJECTED
            CurrentUserMembership.KNOCKED -> MembershipChange.KNOCK_RETRACTED
            else -> MembershipChange.LEFT
        }
        _updates.emit(
            RoomMembershipUpdate(
                roomId = roomId,
                isSpace = isSpace,
                isUserInRoom = false,
                change = membershipChange,
            )
        )
    }
}
