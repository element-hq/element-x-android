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
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

interface KnockRoom {
    suspend operator fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        message: String,
        serverNames: List<String>,
    ): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultKnockRoom(private val client: MatrixClient) : KnockRoom {
    override suspend fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        message: String,
        serverNames: List<String>
    ): Result<Unit> {
        return client
            .knockRoom(roomIdOrAlias, message, serverNames)
            .map { }
    }
}
