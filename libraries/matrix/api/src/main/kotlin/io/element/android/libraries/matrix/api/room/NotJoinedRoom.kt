/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo

/** A reference to a room either invited, knocked or banned. */
interface NotJoinedRoom : AutoCloseable {
    val previewInfo: RoomPreviewInfo
    val localRoom: BaseRoom?

    /**
     * Get the membership details of the user in the room, as well as from the user who sent the `m.room.member` event.
     */
    suspend fun membershipDetails(): Result<RoomMembershipDetails?>
}
