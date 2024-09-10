/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Try to find an existing DM with the given user, or create one if none exists.
 */
suspend fun MatrixClient.startDM(userId: UserId): StartDMResult {
    val existingDM = findDM(userId)
    return if (existingDM != null) {
        StartDMResult.Success(existingDM, isNew = false)
    } else {
        createDM(userId).fold(
            { StartDMResult.Success(it, isNew = true) },
            { StartDMResult.Failure(it) }
        )
    }
}

sealed interface StartDMResult {
    data class Success(val roomId: RoomId, val isNew: Boolean) : StartDMResult
    data class Failure(val throwable: Throwable) : StartDMResult
}
