/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom

private fun Long?.toAnalyticsRoomSize(): JoinedRoom.RoomSize {
    return when (this) {
        null,
        2L -> JoinedRoom.RoomSize.Two
        in 3..10 -> JoinedRoom.RoomSize.ThreeToTen
        in 11..100 -> JoinedRoom.RoomSize.ElevenToOneHundred
        in 101..1000 -> JoinedRoom.RoomSize.OneHundredAndOneToAThousand
        else -> JoinedRoom.RoomSize.MoreThanAThousand
    }
}

fun MatrixRoom.toAnalyticsJoinedRoom(trigger: JoinedRoom.Trigger?): JoinedRoom {
    return JoinedRoom(
        isDM = isDirect,
        isSpace = isSpace,
        roomSize = joinedMemberCount.toAnalyticsRoomSize(),
        trigger = trigger
    )
}
