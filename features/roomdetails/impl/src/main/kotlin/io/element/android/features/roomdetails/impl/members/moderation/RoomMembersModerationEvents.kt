/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember

sealed interface RoomMembersModerationEvents {
    data class SelectRoomMember(val roomMember: RoomMember) : RoomMembersModerationEvents
    data object KickUser : RoomMembersModerationEvents
    data object BanUser : RoomMembersModerationEvents
    data class UnbanUser(val userId: UserId) : RoomMembersModerationEvents
    data object Reset : RoomMembersModerationEvents
}
