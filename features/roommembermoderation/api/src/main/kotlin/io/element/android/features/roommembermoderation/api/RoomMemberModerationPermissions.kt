/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class RoomMemberModerationPermissions(
    val canKick: Boolean,
    val canBan: Boolean,
) {
    // Unban requires both kick and ban permission instead of a dedicated unban permission
    val canUnban = canBan && canKick

    companion object {
        val DEFAULT = RoomMemberModerationPermissions(
            canKick = false,
            canBan = false,
        )
    }
}

fun RoomPermissions.roomMemberModerationPermissions(): RoomMemberModerationPermissions {
    return RoomMemberModerationPermissions(
        canKick = canOwnUserKick(),
        canBan = canOwnUserBan(),
    )
}
