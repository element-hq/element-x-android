/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.test

import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemorySeenInvitesStore(
    initialRoomIds: Set<RoomId> = emptySet(),
) : SeenInvitesStore {
    private val roomIds = MutableStateFlow(initialRoomIds)

    override fun seenRoomIds(): Flow<Set<RoomId>> = roomIds

    override suspend fun markAsSeen(roomId: RoomId) {
        roomIds.value += roomId
    }

    override suspend fun markAsUnSeen(roomId: RoomId) {
        roomIds.value -= roomId
    }

    override suspend fun clear() {
        roomIds.value = emptySet()
    }
}
