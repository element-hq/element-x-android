/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttRoomService
import io.element.android.libraries.di.RoomScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * INTERIM room-scoped implementation: keeps the "PTT enabled" flag in memory for the lifetime of
 * the room session. Toggled from the room settings screen, read by the in-room UI (header/banner/
 * composer). Resets on process death and is NOT shared across users/devices — the productionised
 * version will persist to the `io.element.ptt.config` room-state event once the SDK exposes
 * custom-state read-back.
 */
@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
@Inject
class DefaultPttRoomService : PttRoomService {
    private val isEnabled = MutableStateFlow(false)

    override fun isPttEnabledFlow(): Flow<Boolean> = isEnabled.asStateFlow()

    override suspend fun setPttEnabled(enabled: Boolean) {
        isEnabled.update { enabled }
    }
}
