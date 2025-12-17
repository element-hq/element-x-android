/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.ui.model.roleOf

@Composable
fun BaseRoom.userPowerLevelAsState(updateKey: Long): State<Long> {
    return produceState(initialValue = 0, key1 = updateKey) {
        value = userRole(sessionId)
            .getOrDefault(RoomMember.Role.User)
            .powerLevel
    }
}

@Composable
fun BaseRoom.isOwnUserAdmin(): Boolean {
    val roomInfo by roomInfoFlow.collectAsState()
    val role = roomInfo.roleOf(sessionId)
    return role == RoomMember.Role.Admin || role is RoomMember.Role.Owner
}
