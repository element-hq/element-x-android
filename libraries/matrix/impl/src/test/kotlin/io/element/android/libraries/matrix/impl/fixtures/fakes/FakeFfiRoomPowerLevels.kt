/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomPowerLevels
import org.matrix.rustcomponents.sdk.RoomPowerLevelsValues

class FakeFfiRoomPowerLevels(
    private val values: RoomPowerLevelsValues = defaultFfiRoomPowerLevelValues(),
    private val users: Map<String, Long> = emptyMap(),
) : RoomPowerLevels(NoHandle) {
    override fun values(): RoomPowerLevelsValues = values
    override fun userPowerLevels(): Map<String, Long> = users
}

fun defaultFfiRoomPowerLevelValues() = RoomPowerLevelsValues(
    ban = 50,
    invite = 0,
    kick = 50,
    eventsDefault = 0,
    redact = 50,
    roomName = 100,
    roomAvatar = 100,
    roomTopic = 100,
    stateDefault = 0,
    usersDefault = 0,
    spaceChild = 100,
)
