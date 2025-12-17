/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import kotlinx.coroutines.flow.first

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
 * Returns whether the [BaseRoom] is a DM, with an updated state from the latest [RoomInfo].
 */
suspend fun BaseRoom.isDm() = roomInfoFlow.first().isDm

/**
 * Returns whether the [RoomInfo] is from a DM.
 */
val RoomInfo.isDm get() = isDm(isDirect, activeMembersCount.toInt())
