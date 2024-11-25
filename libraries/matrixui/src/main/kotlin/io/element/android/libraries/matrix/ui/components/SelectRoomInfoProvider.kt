/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class SelectRoomInfoProvider : PreviewParameterProvider<SelectRoomInfo> {
    override val values: Sequence<SelectRoomInfo>
        get() = sequenceOf(
            aSelectRoomInfo(roomId = RoomId("!room1:domain")),
            aSelectRoomInfo(roomId = RoomId("!room2:domain"), name = "Room with a name"),
            aSelectRoomInfo(roomId = RoomId("!room3:domain"), name = "Room with a name and alias", canonicalAlias = RoomAlias("#alias:domain")),
        )
}

fun aSelectRoomInfo(
    roomId: RoomId,
    name: String? = null,
    canonicalAlias: RoomAlias? = null,
    avatarUrl: String? = null,
    heroes: ImmutableList<MatrixUser> = persistentListOf(),
) = SelectRoomInfo(
    roomId = roomId,
    name = name,
    canonicalAlias = canonicalAlias,
    avatarUrl = avatarUrl,
    heroes = heroes,
)
