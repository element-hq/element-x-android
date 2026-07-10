/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.test

import io.element.android.features.ptt.api.PttEnabledSource
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePttEnabledSource(
    initialEnabledRoomIds: Set<RoomId> = emptySet(),
) : PttEnabledSource {
    val enabledRoomIds = MutableStateFlow(initialEnabledRoomIds)

    override fun enabledRoomIdsFlow(sessionId: SessionId): Flow<Set<RoomId>> = enabledRoomIds

    override suspend fun isPttEnabled(sessionId: SessionId, roomId: RoomId): Boolean =
        enabledRoomIds.value.contains(roomId)

    override suspend fun setEnabled(sessionId: SessionId, roomId: RoomId, enabled: Boolean) {
        enabledRoomIds.value = if (enabled) {
            enabledRoomIds.value + roomId
        } else {
            enabledRoomIds.value - roomId
        }
    }
}
