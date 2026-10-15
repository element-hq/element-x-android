/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.ptt.api.PttFeatureAvailability
import io.element.android.features.ptt.api.PttRoomService
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

/**
 * Room-scoped facade over the session-scoped [PttEnabledStore], reading/writing the current room's
 * PTT-enabled flag by room id. Held via the session store (not room-scoped state) so the setting
 * survives navigating in and out of the room — the room graph is recreated per room-open.
 */
@ContributesBinding(RoomScope::class)
class DefaultPttRoomService(
    private val room: JoinedRoom,
    private val store: PttEnabledStore,
    private val availability: PttFeatureAvailability,
) : PttRoomService {
    override fun isPttAvailableFlow(): Flow<Boolean> =
        combine(store.availableRoomIds(), availability.isAvailable) { rooms, available -> available && room.roomId in rooms }
            .distinctUntilChanged()

    override suspend fun setPttAvailable(available: Boolean) {
        if (!available || availability.isAvailable.first()) store.setAvailable(room.roomId, available)
    }

    override fun isPttEnabledFlow(): Flow<Boolean> =
        combine(store.enabledRoomIds(), availability.isAvailable) { rooms, available -> available && room.roomId in rooms }
            .distinctUntilChanged()

    override suspend fun setPttEnabled(enabled: Boolean) {
        if (!enabled || availability.isAvailable.first()) store.setEnabled(room.roomId, enabled)
    }
}
