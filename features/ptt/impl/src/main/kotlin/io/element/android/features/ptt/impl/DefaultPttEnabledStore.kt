/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttEnabledSource
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow

/**
 * Session-scoped facade over the app-scoped [PttEnabledSource], binding the current session id so
 * room-scoped callers ([DefaultPttRoomService]) don't have to thread it through.
 */
@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
@Inject
class DefaultPttEnabledStore(
    matrixClient: MatrixClient,
    private val source: PttEnabledSource,
) : PttEnabledStore {
    private val sessionId = matrixClient.sessionId

    override fun enabledRoomIds(): Flow<Set<RoomId>> = source.enabledRoomIdsFlow(sessionId)

    override suspend fun setEnabled(roomId: RoomId, enabled: Boolean) =
        source.setEnabled(sessionId, roomId, enabled)
}
