/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import org.matrix.rustcomponents.sdk.PowerLevel
import org.matrix.rustcomponents.sdk.RoomPowerLevelsValues as RustRoomPowerLevelsValues

object RoomPowerLevelsValuesMapper {
    fun map(values: RustRoomPowerLevelsValues): RoomPowerLevelsValues {
        return RoomPowerLevelsValues(
            ban = values.ban,
            invite = values.invite,
            kick = values.kick,
            sendEvents = values.eventsDefault,
            redactEvents = values.redact,
            roomName = values.roomName,
            roomAvatar = values.roomAvatar,
            roomTopic = values.roomTopic,
            spaceChild = values.spaceChild,
        )
    }
}

fun PowerLevel.into(): Long = when (this) {
    PowerLevel.Infinite -> RoomMember.Role.Owner(isCreator = true).powerLevel
    is PowerLevel.Value -> this.value
}
