/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_RAW_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

fun aRoomSummary(
    info: MatrixRoomInfo = aRoomInfo(),
    lastMessage: RoomMessage? = aRoomMessage(),
) = RoomSummary(
    info = info,
    lastMessage = lastMessage,
)

fun aRoomSummary(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    rawName: String? = A_ROOM_RAW_NAME,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = null,
    isDirect: Boolean = false,
    isPublic: Boolean = true,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
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
    userPowerLevels: ImmutableMap<UserId, Long> = persistentMapOf(),
    activeRoomCallParticipants: List<UserId> = emptyList(),
    heroes: List<MatrixUser> = emptyList(),
    pinnedEventIds: List<EventId> = emptyList(),
    roomCreator: UserId? = null,
    isMarkedUnread: Boolean = false,
    numUnreadMessages: Long = 0,
    numUnreadNotifications: Long = 0,
    numUnreadMentions: Long = 0,
    lastMessage: RoomMessage? = aRoomMessage(),
) = RoomSummary(
    info = MatrixRoomInfo(
        id = roomId,
        name = name,
        rawName = rawName,
        topic = topic,
        avatarUrl = avatarUrl,
        isDirect = isDirect,
        isPublic = isPublic,
        isSpace = isSpace,
        isTombstoned = isTombstoned,
        isFavorite = isFavorite,
        canonicalAlias = canonicalAlias,
        alternativeAliases = alternativeAliases.toPersistentList(),
        currentUserMembership = currentUserMembership,
        inviter = inviter,
        activeMembersCount = activeMembersCount,
        invitedMembersCount = invitedMembersCount,
        joinedMembersCount = joinedMembersCount,
        userPowerLevels = userPowerLevels,
        highlightCount = highlightCount,
        notificationCount = notificationCount,
        userDefinedNotificationMode = userDefinedNotificationMode,
        hasRoomCall = hasRoomCall,
        activeRoomCallParticipants = activeRoomCallParticipants.toPersistentList(),
        heroes = heroes.toPersistentList(),
        pinnedEventIds = pinnedEventIds.toPersistentList(),
        creator = roomCreator,
        isMarkedUnread = isMarkedUnread,
        numUnreadMessages = numUnreadMessages,
        numUnreadNotifications = numUnreadNotifications,
        numUnreadMentions = numUnreadMentions,
    ),
    lastMessage = lastMessage,
)

fun aRoomMessage(
    eventId: EventId = AN_EVENT_ID,
    event: EventTimelineItem = anEventTimelineItem(),
    userId: UserId = A_USER_ID,
    timestamp: Long = 0L,
) = RoomMessage(
    eventId = eventId,
    event = event,
    sender = userId,
    originServerTs = timestamp,
)
