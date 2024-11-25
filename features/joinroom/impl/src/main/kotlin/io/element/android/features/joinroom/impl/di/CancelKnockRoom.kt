/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import javax.inject.Inject

interface CancelKnockRoom {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultCancelKnockRoom @Inject constructor(private val client: MatrixClient) : CancelKnockRoom {
    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        return client
            .getPendingRoom(roomId)
            ?.leave()
            ?: Result.failure(IllegalStateException("No pending room found"))
    }
}
