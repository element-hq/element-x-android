/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom

class SpaceRoomProvider : PreviewParameterProvider<SpaceRoom> {
    override val values: Sequence<SpaceRoom> = sequenceOf(
        aSpaceRoom(),
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
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            roomId = RoomId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
    )
}
