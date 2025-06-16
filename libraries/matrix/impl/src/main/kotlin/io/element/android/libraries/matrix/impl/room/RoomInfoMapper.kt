/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.SuccessorRoom
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.room.history.map
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomHero
import uniffi.matrix_sdk_base.EncryptionState
import org.matrix.rustcomponents.sdk.Membership as RustMembership
import org.matrix.rustcomponents.sdk.RoomInfo as RustRoomInfo
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode
import org.matrix.rustcomponents.sdk.SuccessorRoom as RustSuccessorRoom

class RoomInfoMapper {
    fun map(rustRoomInfo: RustRoomInfo): RoomInfo = rustRoomInfo.let {
        return RoomInfo(
            id = RoomId(it.id),
            creator = it.creator?.let(::UserId),
            name = it.displayName,
            rawName = it.rawName,
            topic = it.topic,
            avatarUrl = it.avatarUrl,
            isPublic = it.isPublic,
            isDirect = it.isDirect,
            isEncrypted = when (it.encryptionState) {
                EncryptionState.ENCRYPTED -> true
                EncryptionState.NOT_ENCRYPTED -> false
                EncryptionState.UNKNOWN -> null
            },
            joinRule = it.joinRule?.map(),
            isSpace = it.isSpace,
            isFavorite = it.isFavourite,
            canonicalAlias = it.canonicalAlias?.let(::RoomAlias),
            alternativeAliases = it.alternativeAliases.map(::RoomAlias).toImmutableList(),
            currentUserMembership = it.membership.map(),
            inviter = it.inviter?.let(RoomMemberMapper::map),
            activeMembersCount = it.activeMembersCount.toLong(),
            invitedMembersCount = it.invitedMembersCount.toLong(),
            joinedMembersCount = it.joinedMembersCount.toLong(),
            userPowerLevels = mapPowerLevels(it.userPowerLevels),
            highlightCount = it.highlightCount.toLong(),
            notificationCount = it.notificationCount.toLong(),
            userDefinedNotificationMode = it.cachedUserDefinedNotificationMode?.map(),
            hasRoomCall = it.hasRoomCall,
            activeRoomCallParticipants = it.activeRoomCallParticipants.map(::UserId).toImmutableList(),
            heroes = it.elementHeroes().toImmutableList(),
            pinnedEventIds = it.pinnedEventIds.map(::EventId).toImmutableList(),
            isMarkedUnread = it.isMarkedUnread,
            numUnreadMessages = it.numUnreadMessages.toLong(),
            numUnreadMentions = it.numUnreadMentions.toLong(),
            numUnreadNotifications = it.numUnreadNotifications.toLong(),
            historyVisibility = it.historyVisibility.map(),
            successorRoom = it.successorRoom?.map(),
        )
    }
}

fun RustMembership.map(): CurrentUserMembership = when (this) {
    RustMembership.INVITED -> CurrentUserMembership.INVITED
    RustMembership.JOINED -> CurrentUserMembership.JOINED
    RustMembership.LEFT -> CurrentUserMembership.LEFT
    Membership.KNOCKED -> CurrentUserMembership.KNOCKED
    RustMembership.BANNED -> CurrentUserMembership.BANNED
}

fun RustRoomNotificationMode.map(): RoomNotificationMode = when (this) {
    RustRoomNotificationMode.ALL_MESSAGES -> RoomNotificationMode.ALL_MESSAGES
    RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
    RustRoomNotificationMode.MUTE -> RoomNotificationMode.MUTE
}

fun RustSuccessorRoom.map(): SuccessorRoom = SuccessorRoom(
    roomId = RoomId(roomId),
    reason = reason,
)

/**
 * Map a RoomHero to a MatrixUser. There is not need to create a RoomHero type on the application side.
 */
fun RoomHero.map(): MatrixUser = MatrixUser(
    userId = UserId(userId),
    displayName = displayName,
    avatarUrl = avatarUrl
)

fun mapPowerLevels(powerLevels: Map<String, Long>): ImmutableMap<UserId, Long> {
    return powerLevels.mapKeys { (key, _) -> UserId(key) }.toPersistentMap()
}
