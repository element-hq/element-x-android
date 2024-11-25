/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.alias

import io.element.android.libraries.matrix.api.core.RoomAlias

interface RoomAliasHelper {
    fun roomAliasNameFromRoomDisplayName(name: String): String
    fun isRoomAliasValid(roomAlias: RoomAlias): Boolean
}
