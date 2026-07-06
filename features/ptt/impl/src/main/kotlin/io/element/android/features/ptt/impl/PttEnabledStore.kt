/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow

/**
 * Session-scoped persistent store of the rooms that have PTT enabled.
 *
 * INTERIM: this is the local-device gate for the in-room PTT UI, persisted so the setting survives
 * room navigation and process death. The productionised version will read/write the
 * `io.element.ptt.config` room-state event once the SDK exposes custom-state read-back.
 */
interface PttEnabledStore {
    fun enabledRoomIds(): Flow<Set<RoomId>>

    suspend fun setEnabled(roomId: RoomId, enabled: Boolean)
}
