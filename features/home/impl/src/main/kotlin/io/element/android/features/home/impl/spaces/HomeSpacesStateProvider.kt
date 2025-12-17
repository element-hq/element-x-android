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
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

open class HomeSpacesStateProvider : PreviewParameterProvider<HomeSpacesState> {
    override val values: Sequence<HomeSpacesState>
        get() = sequenceOf(
            aHomeSpacesState(
                spaceRooms = SpaceRoomProvider().values.toList(),
                seenSpaceInvites = setOf(
                    RoomId("!spaceId3:example.com"),
                ),
            ),
            aHomeSpacesState(
                space = CurrentSpace.Space(
                    spaceRoom = aSpaceRoom(roomId = RoomId("!mySpace:example.com"))
                ),
                spaceRooms = aListOfSpaceRooms(),
            ),
        )
}

internal fun aHomeSpacesState(
    space: CurrentSpace = CurrentSpace.Root,
    spaceRooms: List<SpaceRoom> = aListOfSpaceRooms(),
    seenSpaceInvites: Set<RoomId> = emptySet(),
    hideInvitesAvatar: Boolean = false,
    eventSink: (HomeSpacesEvents) -> Unit = {},
) = HomeSpacesState(
    space = space,
    spaceRooms = spaceRooms.toImmutableList(),
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    eventSink = eventSink,
)

fun aListOfSpaceRooms(): List<SpaceRoom> {
    return listOf(
        aSpaceRoom(roomId = RoomId("!spaceId0:example.com")),
        aSpaceRoom(roomId = RoomId("!spaceId1:example.com")),
        aSpaceRoom(roomId = RoomId("!spaceId2:example.com")),
    )
}
