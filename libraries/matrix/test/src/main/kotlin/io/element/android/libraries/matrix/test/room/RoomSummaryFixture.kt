/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_RAW_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

fun aRoomSummary(
    info: RoomInfo = aRoomInfo(),
    latestEventValue: LatestEventValue = aRemoteLatestEvent(),
) = RoomSummary(
    info = info,
    latestEvent = latestEventValue,
)

fun aRoomSummary(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    rawName: String? = A_ROOM_RAW_NAME,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = null,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    isEncrypted: Boolean = false,
    joinRule: JoinRule? = JoinRule.Public,
    isSpace: Boolean = false,
    successorRoom: SuccessorRoom? = null,
    isFavorite: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    alternativeAliases: List<RoomAlias> = emptyList(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 1,
    invitedMembersCount: Long = 0,
    joinedMembersCount: Long = 1,
    highlightCount: Long = 0,
    notificationCount: Long = 0,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    roomPowerLevels: RoomPowerLevels = RoomPowerLevels(
        values = defaultRoomPowerLevelValues(),
        users = persistentMapOf(),
    ),
    activeRoomCallParticipants: List<UserId> = emptyList(),
    heroes: List<MatrixUser> = emptyList(),
    pinnedEventIds: List<EventId> = emptyList(),
    roomCreators: List<UserId> = emptyList(),
    isMarkedUnread: Boolean = false,
    numUnreadMessages: Long = 0,
    numUnreadNotifications: Long = 0,
    numUnreadMentions: Long = 0,
    historyVisibility: RoomHistoryVisibility = RoomHistoryVisibility.Joined,
    latestEvent: LatestEventValue = aRemoteLatestEvent(),
    roomVersion: String? = "11",
    privilegedCreatorRole: Boolean = false,
) = RoomSummary(
    info = RoomInfo(
        id = roomId,
        name = name,
        rawName = rawName,
        topic = topic,
        avatarUrl = avatarUrl,
        isPublic = isPublic,
        isDirect = isDirect,
        isEncrypted = isEncrypted,
        joinRule = joinRule,
        isSpace = isSpace,
        successorRoom = successorRoom,
        isFavorite = isFavorite,
        canonicalAlias = canonicalAlias,
        alternativeAliases = alternativeAliases.toImmutableList(),
        currentUserMembership = currentUserMembership,
        inviter = inviter,
        activeMembersCount = activeMembersCount,
        invitedMembersCount = invitedMembersCount,
        joinedMembersCount = joinedMembersCount,
        roomPowerLevels = roomPowerLevels,
        highlightCount = highlightCount,
        notificationCount = notificationCount,
        userDefinedNotificationMode = userDefinedNotificationMode,
        hasRoomCall = hasRoomCall,
        activeRoomCallParticipants = activeRoomCallParticipants.toImmutableList(),
        heroes = heroes.toImmutableList(),
        pinnedEventIds = pinnedEventIds.toImmutableList(),
        creators = roomCreators.toImmutableList(),
        isMarkedUnread = isMarkedUnread,
        numUnreadMessages = numUnreadMessages,
        numUnreadNotifications = numUnreadNotifications,
        numUnreadMentions = numUnreadMentions,
        historyVisibility = historyVisibility,
        roomVersion = roomVersion,
        privilegedCreatorRole = privilegedCreatorRole,
    ),
    latestEvent = latestEvent,
)
