/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom

class SpaceRoomProvider : PreviewParameterProvider<SpaceRoom> {
    override val values: Sequence<SpaceRoom> = sequenceOf(
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name with topic",
            topic = "Room topic that is quite long and might be truncated"
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name no topic",
            state = CurrentUserMembership.LEFT,
        ),
        aSpaceRoom(
            displayName = "Alice",
            roomType = RoomType.Room,
            isDirect = true,
            heroes = listOf(aMatrixUser(displayName = "Alice")),
            state = CurrentUserMembership.JOINED,
            numJoinedMembers = 2,
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name with topic",
            topic = "Room topic that is quite long and might be truncated",
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name no topic",
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            roomId = RoomId("!spaceId0:example.com"),
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            roomId = RoomId("!spaceId1:example.com"),
            state = CurrentUserMembership.LEFT,
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            roomId = RoomId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            displayName = "Alice",
            roomType = RoomType.Space,
            heroes = listOf(aMatrixUser(displayName = "Alice")),
            state = CurrentUserMembership.JOINED,
            numJoinedMembers = 2,
        ),
    )
}
