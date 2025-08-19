/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.user.MatrixUser

class SpaceRoomProvider : PreviewParameterProvider<SpaceRoom> {
    override val values: Sequence<SpaceRoom> = sequenceOf(
        aSpaceRooms(),
        aSpaceRooms(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            spaceId = SpaceId("!spaceId0:example.com"),
        ),
        aSpaceRooms(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            spaceId = SpaceId("!spaceId1:example.com"),
        ),
        aSpaceRooms(
            name = null,
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            spaceId = SpaceId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRooms(
            name = null,
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            spaceId = SpaceId("!spaceId3:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
    )
}

fun aSpaceRooms(
    name: String? = "Space name",
    avatarUrl: String? = null,
    canonicalAlias: RoomAlias? = null,
    childrenCount: Int = 0,
    guestCanJoin: Boolean = false,
    heroes: List<MatrixUser> = emptyList(),
    joinRule: JoinRule? = null,
    numJoinedMembers: Int = 0,
    spaceId: SpaceId = SpaceId("!spaceId:example.com"),
    roomType: RoomType = RoomType.Space,
    state: CurrentUserMembership? = null,
    topic: String? = null,
    worldReadable: Boolean = false,
) = SpaceRoom(
    name = name,
    avatarUrl = avatarUrl,
    canonicalAlias = canonicalAlias,
    childrenCount = childrenCount,
    guestCanJoin = guestCanJoin,
    heroes = heroes,
    joinRule = joinRule,
    numJoinedMembers = numJoinedMembers,
    spaceId = spaceId,
    roomType = roomType,
    state = state,
    topic = topic,
    worldReadable = worldReadable
)
