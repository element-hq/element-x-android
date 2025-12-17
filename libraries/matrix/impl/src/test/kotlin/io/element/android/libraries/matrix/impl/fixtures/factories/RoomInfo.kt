/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomPowerLevels
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import org.matrix.rustcomponents.sdk.JoinRule
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomHero
import org.matrix.rustcomponents.sdk.RoomHistoryVisibility
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomNotificationMode
import org.matrix.rustcomponents.sdk.RoomPowerLevels
import org.matrix.rustcomponents.sdk.SuccessorRoom
import uniffi.matrix_sdk_base.EncryptionState

fun aRustRoomInfo(
    id: String = A_ROOM_ID.value,
    displayName: String? = A_ROOM_NAME,
    rawName: String? = A_ROOM_NAME,
    topic: String? = null,
    avatarUrl: String? = null,
    encryptionState: EncryptionState = EncryptionState.UNKNOWN,
    isDirect: Boolean = false,
    isPublic: Boolean = false,
    isSpace: Boolean = false,
    isFavourite: Boolean = false,
    canonicalAlias: String? = null,
    alternativeAliases: List<String> = listOf(),
    membership: Membership = Membership.JOINED,
    inviter: RoomMember? = null,
    heroes: List<RoomHero> = listOf(),
    activeMembersCount: ULong = 0uL,
    invitedMembersCount: ULong = 0uL,
    joinedMembersCount: ULong = 0uL,
    roomPowerLevels: RoomPowerLevels = FakeFfiRoomPowerLevels(),
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
    roomCreators: List<String>? = emptyList(),
    joinRule: JoinRule? = null,
    historyVisibility: RoomHistoryVisibility = RoomHistoryVisibility.Joined,
    successorRoom: SuccessorRoom? = null,
    roomVersion: String? = "11",
    privilegedCreatorsRole: Boolean = false,
) = RoomInfo(
    id = id,
    displayName = displayName,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    encryptionState = encryptionState,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isFavourite = isFavourite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    membership = membership,
    inviter = inviter,
    heroes = heroes,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    powerLevels = roomPowerLevels,
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
    creators = roomCreators,
    joinRule = joinRule,
    historyVisibility = historyVisibility,
    successorRoom = successorRoom,
    roomVersion = roomVersion,
    privilegedCreatorsRole = privilegedCreatorsRole,
)
