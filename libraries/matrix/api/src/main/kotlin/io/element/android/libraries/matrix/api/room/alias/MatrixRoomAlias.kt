/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.alias

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom

/**
 * Return true if the given roomIdOrAlias is the same room as this room.
 */
fun MatrixRoom.matches(roomIdOrAlias: RoomIdOrAlias): Boolean {
    return when (roomIdOrAlias) {
        is RoomIdOrAlias.Id -> {
            roomIdOrAlias.roomId == roomId
        }
        is RoomIdOrAlias.Alias -> {
            roomIdOrAlias.roomAlias == alias || roomIdOrAlias.roomAlias in alternativeAliases
        }
    }
}
