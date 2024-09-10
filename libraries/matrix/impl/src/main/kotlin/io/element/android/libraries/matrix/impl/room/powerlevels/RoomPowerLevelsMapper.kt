/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import org.matrix.rustcomponents.sdk.RoomPowerLevels as RustRoomPowerLevels

object RoomPowerLevelsMapper {
    fun map(roomPowerLevels: RustRoomPowerLevels): MatrixRoomPowerLevels {
        return MatrixRoomPowerLevels(
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
