/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.model

import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class MediaPermissions(
    val canRedactOwn: Boolean,
    val canRedactOther: Boolean,
) {
    companion object {
        val DEFAULT = MediaPermissions(
            canRedactOwn = false,
            canRedactOther = false,
        )
    }
}

fun RoomPermissions.mediaPermissions(): MediaPermissions {
    return MediaPermissions(
        canRedactOwn = canOwnUserRedactOwn(),
        canRedactOther = canOwnUserRedactOther(),
    )
}
