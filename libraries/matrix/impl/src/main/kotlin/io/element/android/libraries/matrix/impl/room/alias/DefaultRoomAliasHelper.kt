/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.alias

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRoomAliasHelper @Inject constructor() : RoomAliasHelper {
    override fun roomAliasNameFromRoomDisplayName(name: String): String {
        return org.matrix.rustcomponents.sdk.roomAliasNameFromRoomDisplayName(name)
    }

    override fun isRoomAliasValid(roomAlias: RoomAlias): Boolean {
        return org.matrix.rustcomponents.sdk.isRoomAliasFormatValid(roomAlias.value)
    }
}
