/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.alias

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.BaseRoom

/**
 * Return true if the given roomIdOrAlias is the same room as this room.
 */
fun BaseRoom.matches(roomIdOrAlias: RoomIdOrAlias): Boolean {
    return when (roomIdOrAlias) {
        is RoomIdOrAlias.Id -> {
            roomIdOrAlias.roomId == roomId
        }
        is RoomIdOrAlias.Alias -> {
            roomIdOrAlias.roomAlias == info().canonicalAlias || roomIdOrAlias.roomAlias in info().alternativeAliases
        }
    }
}
