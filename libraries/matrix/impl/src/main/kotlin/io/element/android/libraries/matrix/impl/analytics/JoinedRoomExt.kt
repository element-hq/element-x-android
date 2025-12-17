/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.isDm
import kotlinx.coroutines.flow.first

private fun Long.toAnalyticsRoomSize(): JoinedRoom.RoomSize {
    return when (this) {
        0L,
        1L -> JoinedRoom.RoomSize.One
        2L -> JoinedRoom.RoomSize.Two
        in 3..10 -> JoinedRoom.RoomSize.ThreeToTen
        in 11..100 -> JoinedRoom.RoomSize.ElevenToOneHundred
        in 101..1000 -> JoinedRoom.RoomSize.OneHundredAndOneToAThousand
        else -> JoinedRoom.RoomSize.MoreThanAThousand
    }
}

suspend fun BaseRoom.toAnalyticsJoinedRoom(trigger: JoinedRoom.Trigger?): JoinedRoom {
    val roomInfo = roomInfoFlow.first()
    return roomInfo.toAnalyticsJoinedRoom(trigger)
}

fun RoomInfo.toAnalyticsJoinedRoom(trigger: JoinedRoom.Trigger?): JoinedRoom {
    return JoinedRoom(
        isDM = isDm,
        isSpace = isSpace,
        roomSize = joinedMembersCount.toAnalyticsRoomSize(),
        trigger = trigger
    )
}
