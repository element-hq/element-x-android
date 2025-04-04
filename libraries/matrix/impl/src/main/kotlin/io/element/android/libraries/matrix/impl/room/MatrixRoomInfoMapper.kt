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
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
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

class MatrixRoomInfoMapper {
    fun map(rustRoomInfo: RustRoomInfo): MatrixRoomInfo = rustRoomInfo.let {
        return MatrixRoomInfo(
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
            isTombstoned = it.isTombstoned,
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
        )
    }

//    fun map(rustRoom: Room): MatrixRoomInfo = with(rustRoom) {
//        return MatrixRoomInfo(
//            id = RoomId(id()),
//            name = rawName(),
//            rawName = displayName(),
//            topic = topic(),
//            avatarUrl = avatarUrl(),
//            isPublic = isPublic(),
//            isDirect = null,
//            isEncrypted = encryptionState() == EncryptionState.ENCRYPTED,
//            joinRule = null,
//            isSpace = isSpace(),
//            isTombstoned = isTombstoned(),
//            isFavorite = null,
//            canonicalAlias = canonicalAlias()?.let(::RoomAlias),
//            alternativeAliases = alternativeAliases().map(::RoomAlias).toImmutableList(),
//            currentUserMembership = membership().map(),
//            inviter = null,
//            activeMembersCount = activeMembersCount().toLong(),
//            invitedMembersCount = invitedMembersCount().toLong(),
//            joinedMembersCount = joinedMembersCount().toLong(),
//            userPowerLevels = persistentMapOf(),
//            highlightCount = 0,
//            notificationCount = 0,
//            userDefinedNotificationMode = null,
//            hasRoomCall = hasActiveRoomCall(),
//            activeRoomCallParticipants = activeRoomCallParticipants().map(::UserId).toImmutableList(),
//            isMarkedUnread = false,
//            numUnreadMessages = 0,
//            numUnreadNotifications = 0,
//            numUnreadMentions = 0,
//            heroes = heroes().map(RoomHero::map).toImmutableList(),
//            pinnedEventIds = persistentListOf(),
//            creator = null,
//            historyVisibility = null,
//        )
//    }
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

fun mapPowerLevels(powerLevels: Map<String, Long>): ImmutableMap<UserId, Long> {
    return powerLevels.mapKeys { (key, _) -> UserId(key) }.toPersistentMap()
}
