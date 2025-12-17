/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room.alias

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper

class FakeRoomAliasHelper(
    private val roomAliasNameFromRoomDisplayNameLambda: (String) -> String = { name ->
        name.trimStart().trimEnd().replace(" ", "_")
    },
    private val isRoomAliasValidLambda: (RoomAlias) -> Boolean = { true }
) : RoomAliasHelper {
    override fun roomAliasNameFromRoomDisplayName(name: String): String {
        return roomAliasNameFromRoomDisplayNameLambda(name)
    }

    override fun isRoomAliasValid(roomAlias: RoomAlias): Boolean {
        return isRoomAliasValidLambda(roomAlias)
    }
}
