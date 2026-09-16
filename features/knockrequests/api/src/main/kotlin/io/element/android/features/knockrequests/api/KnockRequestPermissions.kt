/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.api

import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class KnockRequestPermissions(
    val canAccept: Boolean,
    val canDecline: Boolean,
    val canBan: Boolean,
) {
    val hasAny = canAccept || canDecline || canBan

    companion object {
        val DEFAULT = KnockRequestPermissions(
            canAccept = false,
            canDecline = false,
            canBan = false,
        )
    }
}

fun RoomPermissions.knockRequestPermissions(): KnockRequestPermissions {
    return KnockRequestPermissions(
        canAccept = canOwnUserInvite(),
        canDecline = canOwnUserKick(),
        canBan = canOwnUserBan(),
    )
}
