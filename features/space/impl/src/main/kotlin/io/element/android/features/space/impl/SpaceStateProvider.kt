/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

open class SpaceStateProvider : PreviewParameterProvider<SpaceState> {
    override val values: Sequence<SpaceState>
        get() = sequenceOf(
            aSpaceState(),
            aSpaceState(
                parentSpace = aSpaceRoom(
                    name = null,
                    numJoinedMembers = 5,
                    childrenCount = 10,
                    worldReadable = true,
                ),
                hasMoreToLoad = true,
            ),
            aSpaceState(
                hasMoreToLoad = true,
                children = aListOfSpaceRooms(),
            ),
            aSpaceState(
                hasMoreToLoad = false,
                children = aListOfSpaceRooms()
            )
            // Add other states here
        )
}

fun aSpaceState(
    parentSpace: SpaceRoom? = aSpaceRoom(
        numJoinedMembers = 5,
        childrenCount = 10,
        worldReadable = true,
        roomId = RoomId("!spaceId0:example.com"),
    ),
    children: List<SpaceRoom> = emptyList(),
    seenSpaceInvites: Set<RoomId> = emptySet(),
    hideInvitesAvatar: Boolean = false,
    hasMoreToLoad: Boolean = false,
) = SpaceState(
    currentSpace = parentSpace,
    children = children.toImmutableList(),
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    hasMoreToLoad = hasMoreToLoad,
    eventSink = {}
)

private fun aListOfSpaceRooms(): List<SpaceRoom> {
    return listOf(
        aSpaceRoom(roomId = RoomId("!spaceId0:example.com")),
        aSpaceRoom(roomId = RoomId("!spaceId1:example.com")),
        aSpaceRoom(roomId = RoomId("!spaceId2:example.com")),
    )
}
