/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import javax.inject.Inject

interface ForgetRoom {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultForgetRoom @Inject constructor(private val client: MatrixClient) : ForgetRoom {
    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        return client.getRoom(roomId)?.use { it.forget() }
            ?: Result.failure(IllegalStateException("Room not found"))
    }
}
