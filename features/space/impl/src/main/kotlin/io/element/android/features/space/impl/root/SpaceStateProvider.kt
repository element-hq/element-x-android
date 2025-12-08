/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

open class SpaceStateProvider : PreviewParameterProvider<SpaceState> {
    override val values: Sequence<SpaceState>
        get() = sequenceOf(
            aSpaceState(),
            aSpaceState(parentSpace = aParentSpace(joinRule = JoinRule.Public)),
            aSpaceState(parentSpace = aParentSpace(joinRule = JoinRule.Restricted(persistentListOf()))),
            aSpaceState(children = aListOfSpaceRooms()),
            aSpaceState(
                parentSpace = aParentSpace(),
                children = aListOfSpaceRooms(),
                joiningRooms = setOf(RoomId("!spaceId0:example.com")),
                hasMoreToLoad = false
            ),
            aSpaceState(
                topicViewerState = TopicViewerState.Shown(topic = "Space description goes here." + LoremIpsum(20).values.first()),
            ),
            // Add other states here
        )
}

fun aSpaceState(
    parentSpace: SpaceRoom? = aParentSpace(),
    children: List<SpaceRoom> = emptyList(),
    seenSpaceInvites: Set<RoomId> = emptySet(),
    joiningRooms: Set<RoomId> = emptySet(),
    joinActions: Map<RoomId, AsyncAction<Unit>> = joiningRooms.associateWith { AsyncAction.Loading },
    hideInvitesAvatar: Boolean = false,
    hasMoreToLoad: Boolean = true,
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    topicViewerState: TopicViewerState = TopicViewerState.Hidden,
    canAccessSpaceSettings: Boolean = true,
    eventSink: (SpaceEvents) -> Unit = { },
) = SpaceState(
    currentSpace = parentSpace,
    children = children.toImmutableList(),
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    hasMoreToLoad = hasMoreToLoad,
    joinActions = joinActions.toImmutableMap(),
    acceptDeclineInviteState = acceptDeclineInviteState,
    topicViewerState = topicViewerState,
    canAccessSpaceSettings = canAccessSpaceSettings,
    eventSink = eventSink,
)

private fun aParentSpace(
    joinRule: JoinRule? = null,
): SpaceRoom {
    return aSpaceRoom(
        numJoinedMembers = 5,
        childrenCount = 10,
        worldReadable = true,
        joinRule = joinRule,
        roomId = RoomId("!spaceId0:example.com"),
        topic = "Space description goes here. " + LoremIpsum(20).values.first(),
    )
}

private fun aListOfSpaceRooms(): List<SpaceRoom> {
    return listOf(
        aSpaceRoom(
            roomId = RoomId("!spaceId0:example.com"),
            state = null,
        ),
        aSpaceRoom(
            roomId = RoomId("!spaceId1:example.com"),
            state = CurrentUserMembership.JOINED,
        ),
        aSpaceRoom(
            roomId = RoomId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
    )
}
