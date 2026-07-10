/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * App-scoped source of the per-room "PTT enabled" gate, keyed by session.
 *
 * App-scoped (not session-scoped) so it can be read from the push-notification pipeline, which
 * resolves sessions on demand and cannot hold a session-scoped binding.
 *
 * INTERIM: backed by per-session device-local storage. The productionised version reads the
 * `io.element.ptt.config` room-state event once the SDK exposes custom-state read-back — behind this
 * same interface.
 */
interface PttEnabledSource {
    fun enabledRoomIdsFlow(sessionId: SessionId): Flow<Set<RoomId>>

    suspend fun isPttEnabled(sessionId: SessionId, roomId: RoomId): Boolean

    suspend fun setEnabled(sessionId: SessionId, roomId: RoomId, enabled: Boolean)
}
