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
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
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
            aSpaceState(spaceInfo = aSpaceInfo(joinRule = JoinRule.Public)),
            aSpaceState(spaceInfo = aSpaceInfo(joinRule = JoinRule.Restricted(persistentListOf()))),
            aSpaceState(children = aListOfSpaceRooms()),
            aSpaceState(
                spaceInfo = aSpaceInfo(),
                children = aListOfSpaceRooms(),
                joiningRooms = setOf(RoomId("!spaceId0:example.com")),
                hasMoreToLoad = true,
            ),
            aSpaceState(
                topicViewerState = TopicViewerState.Shown(topic = "Space description goes here." + LoremIpsum(20).values.first()),
            ),
            // Manage mode states
            aSpaceState(
                spaceInfo = aSpaceInfo(),
                children = aListOfSpaceRooms(),
                isManageMode = true,
                selectedRoomIds = emptySet(),
            ),
            aSpaceState(
                spaceInfo = aSpaceInfo(),
                children = aListOfSpaceRooms(),
                isManageMode = true,
                selectedRoomIds = setOf(RoomId("!spaceId0:example.com"), RoomId("!spaceId1:example.com")),
            ),
            aSpaceState(
                spaceInfo = aSpaceInfo(),
                children = aListOfSpaceRooms(),
                isManageMode = true,
                selectedRoomIds = setOf(RoomId("!spaceId0:example.com")),
                removeRoomsAction = AsyncAction.ConfirmingNoParams,
            ),
        )
}

fun aSpaceState(
    spaceInfo: RoomInfo = aSpaceInfo(),
    children: List<SpaceRoom> = emptyList(),
    seenSpaceInvites: Set<RoomId> = emptySet(),
    joiningRooms: Set<RoomId> = emptySet(),
    joinActions: Map<RoomId, AsyncAction<Unit>> = joiningRooms.associateWith { AsyncAction.Loading },
    hideInvitesAvatar: Boolean = false,
    hasMoreToLoad: Boolean = false,
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    topicViewerState: TopicViewerState = TopicViewerState.Hidden,
    canAccessSpaceSettings: Boolean = true,
    isManageMode: Boolean = false,
    selectedRoomIds: Set<RoomId> = emptySet(),
    canManageRooms: Boolean = true,
    removeRoomsAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (SpaceEvents) -> Unit = { },
) = SpaceState(
    spaceInfo = spaceInfo,
    children = children.toImmutableList(),
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    hasMoreToLoad = hasMoreToLoad,
    joinActions = joinActions.toImmutableMap(),
    acceptDeclineInviteState = acceptDeclineInviteState,
    topicViewerState = topicViewerState,
    canAccessSpaceSettings = canAccessSpaceSettings,
    isManageMode = isManageMode,
    selectedRoomIds = selectedRoomIds.toImmutableSet(),
    canEditSpaceGraph = canManageRooms,
    removeRoomsAction = removeRoomsAction,
    eventSink = eventSink,
)

private fun aSpaceInfo(
    joinRule: JoinRule? = null,
): RoomInfo {
    return RoomInfo(
        id = RoomId("!spaceId0:example.com"),
        name = "A Space",
        rawName = "A Space",
        topic = "Space description goes here. " + LoremIpsum(20).values.first(),
        avatarUrl = null,
        isPublic = true,
        isDirect = false,
        isEncrypted = false,
        joinRule = joinRule,
        isSpace = true,
        isFavorite = false,
        canonicalAlias = null,
        alternativeAliases = persistentListOf(),
        currentUserMembership = CurrentUserMembership.JOINED,
        inviter = null,
        activeMembersCount = 5,
        invitedMembersCount = 0,
        joinedMembersCount = 5,
        roomPowerLevels = null,
        highlightCount = 0,
        notificationCount = 0,
        userDefinedNotificationMode = null,
        hasRoomCall = false,
        activeRoomCallParticipants = persistentListOf(),
        isMarkedUnread = false,
        numUnreadMessages = 0,
        numUnreadNotifications = 0,
        numUnreadMentions = 0,
        heroes = persistentListOf(),
        pinnedEventIds = persistentListOf(),
        creators = persistentListOf(),
        historyVisibility = RoomHistoryVisibility.Joined,
        successorRoom = null,
        roomVersion = "11",
        privilegedCreatorRole = false,
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
