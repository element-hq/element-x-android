/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomHero
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomNotificationMode

fun aRustRoomInfo(
    id: String = A_ROOM_ID.value,
    displayName: String? = A_ROOM_NAME,
    rawName: String? = A_ROOM_NAME,
    topic: String? = null,
    avatarUrl: String? = null,
    isDirect: Boolean = false,
    isPublic: Boolean = false,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
    isFavourite: Boolean = false,
    canonicalAlias: String? = null,
    alternativeAliases: List<String> = listOf(),
    membership: Membership = Membership.JOINED,
    inviter: RoomMember? = null,
    heroes: List<RoomHero> = listOf(),
    activeMembersCount: ULong = 0uL,
    invitedMembersCount: ULong = 0uL,
    joinedMembersCount: ULong = 0uL,
    userPowerLevels: Map<String, Long> = mapOf(),
    highlightCount: ULong = 0uL,
    notificationCount: ULong = 0uL,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    activeRoomCallParticipants: List<String> = listOf(),
    isMarkedUnread: Boolean = false,
    numUnreadMessages: ULong = 0uL,
    numUnreadNotifications: ULong = 0uL,
    numUnreadMentions: ULong = 0uL,
    pinnedEventIds: List<String> = listOf(),
    roomCreator: UserId? = null,
) = RoomInfo(
    id = id,
    displayName = displayName,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isTombstoned = isTombstoned,
    isFavourite = isFavourite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    membership = membership,
    inviter = inviter,
    heroes = heroes,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    userPowerLevels = userPowerLevels,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    cachedUserDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    activeRoomCallParticipants = activeRoomCallParticipants,
    isMarkedUnread = isMarkedUnread,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    numUnreadMentions = numUnreadMentions,
    pinnedEventIds = pinnedEventIds,
    creator = roomCreator?.value,
)
