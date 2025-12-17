/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetailsedit.api

import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class RoomDetailsEditPermissions(
    val canEditName: Boolean,
    val canEditTopic: Boolean,
    val canEditAvatar: Boolean,
) {
    val hasAny = canEditName ||
        canEditTopic ||
        canEditAvatar

    companion object {
        val DEFAULT = RoomDetailsEditPermissions(
            canEditName = false,
            canEditTopic = false,
            canEditAvatar = false,
        )
    }
}

fun RoomPermissions.roomDetailsEditPermissions(): RoomDetailsEditPermissions {
    return RoomDetailsEditPermissions(
        canEditName = canOwnUserSendState(StateEventType.ROOM_NAME),
        canEditTopic = canOwnUserSendState(StateEventType.ROOM_TOPIC),
        canEditAvatar = canOwnUserSendState(StateEventType.ROOM_AVATAR),
    )
}
