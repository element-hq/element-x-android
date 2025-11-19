/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.RoomPowerLevelsValues

internal fun aRustRoomPowerLevelsValues(
    ban: Long,
    invite: Long,
    kick: Long,
    redact: Long,
    eventsDefault: Long,
    stateDefault: Long,
    usersDefault: Long,
    roomName: Long,
    roomAvatar: Long,
    roomTopic: Long,
    spaceChild: Long,
) = RoomPowerLevelsValues(
    ban = ban,
    invite = invite,
    kick = kick,
    redact = redact,
    eventsDefault = eventsDefault,
    stateDefault = stateDefault,
    usersDefault = usersDefault,
    roomName = roomName,
    roomAvatar = roomAvatar,
    roomTopic = roomTopic,
    spaceChild = spaceChild
)
