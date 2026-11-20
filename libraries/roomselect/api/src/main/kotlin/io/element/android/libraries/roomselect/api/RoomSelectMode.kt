/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.api

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface RoomSelectMode {
    /**
     * Forward content from a room to other rooms.
     * @param fromRoomId the room the content is forwarded from. It is excluded from the list of selectable rooms.
     */
    data class Forward(val fromRoomId: RoomId) : RoomSelectMode

    /**
     * Share content coming from outside the application. There is no source room in this mode.
     */
    data object Share : RoomSelectMode
}
