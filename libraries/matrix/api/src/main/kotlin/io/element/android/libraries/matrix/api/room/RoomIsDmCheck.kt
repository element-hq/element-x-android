/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

/**
 * Returns whether the room with the provided info is a DM.
 * A DM is a room with at most 2 active members (one of them may have left).
 *
 * @param isDirect true if the room is direct
 * @param activeMembersCount the number of active members in the room (joined or invited)
 */
fun isDm(isDirect: Boolean, activeMembersCount: Int): Boolean {
    return isDirect && activeMembersCount <= 2
}

/**
 * Returns whether the [MatrixRoom] is a DM.
 */
val MatrixRoom.isDm get() = isDm(isDirect, activeMemberCount.toInt())

/**
 * Returns whether the [MatrixRoomInfo] is from a DM.
 */
val MatrixRoomInfo.isDm get() = isDm(isDirect, activeMembersCount.toInt())
