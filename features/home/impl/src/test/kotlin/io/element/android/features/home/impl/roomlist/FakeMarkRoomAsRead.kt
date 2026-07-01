/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import io.element.android.features.preferences.impl.tasks.MarkRoomAsRead
import io.element.android.libraries.matrix.api.core.RoomId

class FakeMarkRoomAsRead(
    private val invokeLambda: suspend (RoomId) -> Result<Unit> = { Result.success(Unit) },
) : MarkRoomAsRead {
    val invokedRoomIds = mutableListOf<RoomId>()

    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        invokedRoomIds.add(roomId)
        return invokeLambda(roomId)
    }
}
