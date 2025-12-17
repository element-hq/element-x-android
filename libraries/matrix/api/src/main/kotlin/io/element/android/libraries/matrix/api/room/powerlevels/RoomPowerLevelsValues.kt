/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

data class RoomPowerLevelsValues(
    val ban: Long,
    val invite: Long,
    val kick: Long,
    val eventsDefault: Long,
    val stateDefault: Long,
    val redactEvents: Long,
    val roomName: Long,
    val roomAvatar: Long,
    val roomTopic: Long,
    val spaceChild: Long,
)
