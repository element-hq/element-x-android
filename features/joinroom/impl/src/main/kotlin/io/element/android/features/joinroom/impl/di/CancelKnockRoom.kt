/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl.di

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId

interface CancelKnockRoom {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultCancelKnockRoom(private val client: MatrixClient) : CancelKnockRoom {
    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        return client
            .getRoom(roomId)
            ?.use { it.leave() }
            ?: Result.failure(IllegalStateException("No pending room found"))
    }
}
