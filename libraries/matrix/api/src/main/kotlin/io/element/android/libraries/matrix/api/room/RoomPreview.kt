/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/** A reference to a room either invited, knocked or banned. */
interface RoomPreview : AutoCloseable {
    val sessionId: SessionId
    val roomId: RoomId

    /** Leave the room ie.decline invite or cancel knock. */
    suspend fun leave(): Result<Unit>

    /**
     * Forget the room if we had access to it, and it was left or banned.
     */
    suspend fun forget(): Result<Unit>
}
