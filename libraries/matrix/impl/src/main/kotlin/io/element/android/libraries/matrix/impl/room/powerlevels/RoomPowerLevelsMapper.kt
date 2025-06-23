/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import org.matrix.rustcomponents.sdk.RoomPowerLevels as RustRoomPowerLevels

object RoomPowerLevelsMapper {
    fun map(roomPowerLevels: RustRoomPowerLevels): RoomPowerLevels {
        return RoomPowerLevels(
                ban = roomPowerLevels.ban,
                invite = roomPowerLevels.invite,
                kick = roomPowerLevels.kick,
                sendEvents = roomPowerLevels.eventsDefault,
                redactEvents = roomPowerLevels.redact,
                roomName = roomPowerLevels.roomName,
                roomAvatar = roomPowerLevels.roomAvatar,
                roomTopic = roomPowerLevels.roomTopic
        )
    }
}
