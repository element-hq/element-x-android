/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.intent

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.hasJoined
import timber.log.Timber

/**
 * Find the session to use to open a room permalink, when several sessions exist.
 */
interface PermalinkSessionResolver {
    /**
     * Return the first session in [sessionIds] which has joined the room, or null if none of them has.
     *
     * @param roomIdOrAlias the room id or alias from the permalink.
     * @param sessionIds the session ids, ordered from the most recently used to the least recently used.
     */
    suspend fun resolve(roomIdOrAlias: RoomIdOrAlias, sessionIds: List<SessionId>): SessionId?
}

@ContributesBinding(AppScope::class)
class DefaultPermalinkSessionResolver(
    private val matrixClientProvider: MatrixClientProvider,
) : PermalinkSessionResolver {
    override suspend fun resolve(roomIdOrAlias: RoomIdOrAlias, sessionIds: List<SessionId>): SessionId? {
        val clients = sessionIds.mapNotNull { sessionId ->
            matrixClientProvider.getOrRestore(sessionId)
                .onFailure { Timber.w(it, "Unable to restore session $sessionId") }
                .getOrNull()
        }
        val roomId = when (roomIdOrAlias) {
            is RoomIdOrAlias.Id -> roomIdOrAlias.roomId
            is RoomIdOrAlias.Alias -> clients.firstNotNullOfOrNull { client ->
                client.resolveRoomAlias(roomIdOrAlias.roomAlias).getOrNull()?.orElse(null)?.roomId
            }
        }
        if (roomId == null) {
            Timber.d("Unable to resolve ${roomIdOrAlias.identifier}")
            return null
        }
        return clients
            .firstOrNull { client -> client.hasJoined(roomId) }
            ?.sessionId
            .also { Timber.d("Session for room $roomId: $it") }
    }
}
