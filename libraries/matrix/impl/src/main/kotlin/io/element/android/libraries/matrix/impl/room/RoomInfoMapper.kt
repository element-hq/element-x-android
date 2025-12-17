/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.room.history.map
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.powerlevels.RoomPowerLevelsValuesMapper
import io.element.android.libraries.matrix.impl.room.tombstone.map
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomHero
import uniffi.matrix_sdk_base.EncryptionState
import org.matrix.rustcomponents.sdk.Membership as RustMembership
import org.matrix.rustcomponents.sdk.RoomInfo as RustRoomInfo
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode
import org.matrix.rustcomponents.sdk.RoomPowerLevels as RustRoomPowerLevels

class RoomInfoMapper {
    fun map(rustRoomInfo: RustRoomInfo): RoomInfo = rustRoomInfo.let {
        return RoomInfo(
            id = RoomId(it.id),
            creators = it.creators.orEmpty().map(::UserId).toImmutableList(),
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
            roomPowerLevels = it.powerLevels?.let(::mapPowerLevels),
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
            roomVersion = it.roomVersion,
            privilegedCreatorRole = it.privilegedCreatorsRole,
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

/**
 * Map a RoomHero to a MatrixUser. There is not need to create a RoomHero type on the application side.
 */
fun RoomHero.map(): MatrixUser = MatrixUser(
    userId = UserId(userId),
    displayName = displayName,
    avatarUrl = avatarUrl
)

fun mapPowerLevels(roomPowerLevels: RustRoomPowerLevels): RoomPowerLevels {
    return RoomPowerLevels(
        values = RoomPowerLevelsValuesMapper.map(roomPowerLevels.values()),
        users = roomPowerLevels.userPowerLevels().mapKeys { (key, _) -> UserId(key) }.toImmutableMap()
    )
}
