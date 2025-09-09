/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.toImmutableSet

open class HomeSpacesStateProvider : PreviewParameterProvider<HomeSpacesState> {
    override val values: Sequence<HomeSpacesState>
        get() = sequenceOf(
            aHomeSpacesState(
                spaceRooms = SpaceRoomProvider().values.toList(),
                seenSpaceInvites = setOf(
                    SpaceId("!spaceId3:example.com"),
                ),
            ),
            aHomeSpacesState(
                space = CurrentSpace.Space(
                    spaceRoom = aSpaceRooms(spaceId = SpaceId("!mySpace:example.com"))
                ),
                spaceRooms = aListOfSpaceRooms(),
            ),
        )
}

internal fun aHomeSpacesState(
    space: CurrentSpace = CurrentSpace.Root,
    spaceRooms: List<SpaceRoom> = aListOfSpaceRooms(),
    seenSpaceInvites: Set<SpaceId> = emptySet(),
    hideInvitesAvatar: Boolean = false,
    eventSink: (HomeSpacesEvents) -> Unit = {},
) = HomeSpacesState(
    space = space,
    spaceRooms = spaceRooms,
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    eventSink = eventSink,
)

fun aListOfSpaceRooms(): List<SpaceRoom> {
    return listOf(
        aSpaceRooms(spaceId = SpaceId("!spaceId0:example.com")),
        aSpaceRooms(spaceId = SpaceId("!spaceId1:example.com")),
        aSpaceRooms(spaceId = SpaceId("!spaceId2:example.com")),
    )
}
