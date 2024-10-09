/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class SelectRoomInfo(
    val roomId: RoomId,
    val name: String?,
    val canonicalAlias: RoomAlias?,
    val avatarUrl: String?,
    val heroes: ImmutableList<MatrixUser>,
) {
    fun getAvatarData(size: AvatarSize) = AvatarData(
        id = roomId.value,
        name = name,
        url = avatarUrl,
        size = size,
    )
}

fun RoomSummary.toSelectRoomInfo() = SelectRoomInfo(
    roomId = roomId,
    name = info.name,
    avatarUrl = info.avatarUrl,
    heroes = info.heroes,
    canonicalAlias = info.canonicalAlias,
)
