/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.withContext
import timber.log.Timber

interface MarkAsFullyRead {
    suspend operator fun invoke(roomId: RoomId, eventId: EventId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultMarkAsFullyRead(
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MarkAsFullyRead {
    override suspend fun invoke(roomId: RoomId, eventId: EventId): Result<Unit> = withContext(coroutineDispatchers.io) {
        matrixClient.markRoomAsFullyRead(roomId, eventId).onFailure {
            Timber.e(it, "Failed to mark room $roomId as fully read for event $eventId")
        }
    }
}
