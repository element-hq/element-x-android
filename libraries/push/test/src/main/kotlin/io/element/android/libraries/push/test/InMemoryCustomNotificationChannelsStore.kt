/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.store.CustomNotificationChannelsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryCustomNotificationChannelsStore : CustomNotificationChannelsStore {
    private val roomIdsWithCustomChannelFlow = MutableStateFlow<Set<RoomId>>(emptySet())

    override fun roomIdsWithCustomChannel(): Flow<Set<RoomId>> = roomIdsWithCustomChannelFlow

    override suspend fun hasCustomChannel(roomId: RoomId): Boolean {
        return roomIdsWithCustomChannelFlow.value.contains(roomId)
    }

    override suspend fun addCustomChannel(roomId: RoomId) {
        roomIdsWithCustomChannelFlow.value = roomIdsWithCustomChannelFlow.value + roomId
    }

    override suspend fun removeCustomChannel(roomId: RoomId) {
        roomIdsWithCustomChannelFlow.value = roomIdsWithCustomChannelFlow.value - roomId
    }

    override suspend fun clear() {
        roomIdsWithCustomChannelFlow.value = emptySet()
    }
}
