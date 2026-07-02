/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import kotlinx.coroutines.flow.Flow

/**
 * Per-room gate for the Push-to-Talk UI: is PTT enabled in this room?
 *
 * INTERIM: backed by a room-scoped in-memory store toggled from the room settings screen.
 * The productionised version reads/writes the `io.element.ptt.config` room-state event so the
 * setting is shared across users and devices — blocked today by the rust-SDK custom-state
 * read-back gap (see the roadmap plan, Phase 0/2).
 */
interface PttRoomService {
    fun isPttEnabledFlow(): Flow<Boolean>

    suspend fun setPttEnabled(enabled: Boolean)
}
