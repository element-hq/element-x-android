/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.hasJoined
import io.element.android.libraries.sessionstorage.api.SessionStore

/**
 * Check whether a room is also joined by another account signed in on this device.
 */
interface RoomSharedWithOtherAccountChecker {
    suspend fun isJoinedByOtherAccount(sessionId: SessionId, roomId: RoomId): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultRoomSharedWithOtherAccountChecker(
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
) : RoomSharedWithOtherAccountChecker {
    override suspend fun isJoinedByOtherAccount(sessionId: SessionId, roomId: RoomId): Boolean {
        return sessionStore.getAllSessions()
            .map { SessionId(it.userId) }
            .filter { it != sessionId }
            .any { otherSessionId ->
                matrixClientProvider.getOrRestore(otherSessionId).getOrNull()
                    ?.hasJoined(roomId)
                    ?: false
            }
    }
}
