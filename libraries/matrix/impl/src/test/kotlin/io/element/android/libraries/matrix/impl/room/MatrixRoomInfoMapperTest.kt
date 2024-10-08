/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomHero
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomMember
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_6
import io.element.android.libraries.matrix.test.room.aRoomMember
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode

class MatrixRoomInfoMapperTest {
    @Test
    fun `mapping of RustRoomInfo should map all the fields`() {
        assertThat(
            MatrixRoomInfoMapper().map(
                aRustRoomInfo(
                    id = A_ROOM_ID.value,
                    displayName = "displayName",
                    rawName = "rawName",
                    topic = "topic",
                    avatarUrl = AN_AVATAR_URL,
                    isDirect = true,
                    isPublic = false,
                    isSpace = false,
                    isTombstoned = false,
                    isFavourite = false,
                    canonicalAlias = A_ROOM_ALIAS.value,
                    alternativeAliases = listOf(A_ROOM_ALIAS.value),
                    membership = Membership.JOINED,
                    inviter = aRustRoomMember(A_USER_ID),
                    heroes = listOf(aRustRoomHero()),
                    activeMembersCount = 2uL,
                    invitedMembersCount = 3uL,
                    joinedMembersCount = 4uL,
                    userPowerLevels = mapOf(A_USER_ID_6.value to 50L),
                    highlightCount = 10uL,
                    notificationCount = 11uL,
                    userDefinedNotificationMode = RustRoomNotificationMode.MUTE,
                    hasRoomCall = true,
                    activeRoomCallParticipants = listOf(A_USER_ID_3.value),
                    isMarkedUnread = false,
                    numUnreadMessages = 12uL,
                    numUnreadNotifications = 13uL,
                    numUnreadMentions = 14uL,
                    pinnedEventIds = listOf(AN_EVENT_ID.value),
                    roomCreator = A_USER_ID,
                )
            )
        ).isEqualTo(
            MatrixRoomInfo(
                id = A_ROOM_ID,
                name = "displayName",
                rawName = "rawName",
                topic = "topic",
                avatarUrl = AN_AVATAR_URL,
                isDirect = true,
                isPublic = false,
                isSpace = false,
                isTombstoned = false,
                isFavorite = false,
                canonicalAlias = A_ROOM_ALIAS,
                alternativeAliases = listOf(A_ROOM_ALIAS).toImmutableList(),
                currentUserMembership = CurrentUserMembership.JOINED,
                inviter = aRoomMember(A_USER_ID),
                activeMembersCount = 2L,
                invitedMembersCount = 3L,
                joinedMembersCount = 4L,
                userPowerLevels = mapOf(A_USER_ID_6 to 50L).toImmutableMap(),
                highlightCount = 10L,
                notificationCount = 11L,
                userDefinedNotificationMode = RoomNotificationMode.MUTE,
                hasRoomCall = true,
                activeRoomCallParticipants = listOf(A_USER_ID_3).toImmutableList(),
                heroes = listOf(
                    MatrixUser(
                        userId = A_USER_ID,
                        displayName = "displayName",
                        avatarUrl = "avatarUrl",
                    )
                ).toImmutableList(),
                pinnedEventIds = listOf(AN_EVENT_ID).toPersistentList(),
                creator = A_USER_ID,
                isMarkedUnread = false,
                numUnreadMessages = 12L,
                numUnreadNotifications = 13L,
                numUnreadMentions = 14L,
            )
        )
    }

    @Test
    fun `mapping of RustRoomInfo with null members should map all the fields`() {
        assertThat(
            MatrixRoomInfoMapper().map(
                aRustRoomInfo(
                    id = A_ROOM_ID.value,
                    displayName = null,
                    rawName = null,
                    topic = null,
                    avatarUrl = null,
                    isDirect = false,
                    isPublic = true,
                    isSpace = false,
                    isTombstoned = false,
                    isFavourite = true,
                    canonicalAlias = null,
                    alternativeAliases = emptyList(),
                    membership = Membership.INVITED,
                    inviter = null,
                    heroes = listOf(aRustRoomHero()),
                    activeMembersCount = 2uL,
                    invitedMembersCount = 3uL,
                    joinedMembersCount = 4uL,
                    userPowerLevels = emptyMap(),
                    highlightCount = 10uL,
                    notificationCount = 11uL,
                    userDefinedNotificationMode = null,
                    hasRoomCall = false,
                    activeRoomCallParticipants = emptyList(),
                    isMarkedUnread = true,
                    numUnreadMessages = 12uL,
                    numUnreadNotifications = 13uL,
                    numUnreadMentions = 14uL,
                    pinnedEventIds = emptyList(),
                    roomCreator = null,
                )
            )
        ).isEqualTo(
            MatrixRoomInfo(
                id = A_ROOM_ID,
                name = null,
                rawName = null,
                topic = null,
                avatarUrl = null,
                isDirect = false,
                isPublic = true,
                isSpace = false,
                isTombstoned = false,
                isFavorite = true,
                canonicalAlias = null,
                alternativeAliases = emptyList<RoomAlias>().toPersistentList(),
                currentUserMembership = CurrentUserMembership.INVITED,
                inviter = null,
                activeMembersCount = 2L,
                invitedMembersCount = 3L,
                joinedMembersCount = 4L,
                userPowerLevels = emptyMap<UserId, Long>().toImmutableMap(),
                highlightCount = 10L,
                notificationCount = 11L,
                userDefinedNotificationMode = null,
                hasRoomCall = false,
                activeRoomCallParticipants = emptyList<UserId>().toImmutableList(),
                heroes = emptyList<MatrixUser>().toImmutableList(),
                pinnedEventIds = emptyList<EventId>().toPersistentList(),
                creator = null,
                isMarkedUnread = true,
                numUnreadMessages = 12L,
                numUnreadNotifications = 13L,
                numUnreadMentions = 14L,
            )
        )
    }

    @Test
    fun `mapping Membership`() {
        assertThat(Membership.INVITED.map()).isEqualTo(CurrentUserMembership.INVITED)
        assertThat(Membership.JOINED.map()).isEqualTo(CurrentUserMembership.JOINED)
        assertThat(Membership.LEFT.map()).isEqualTo(CurrentUserMembership.LEFT)
    }

    @Test
    fun `mapping RoomNotificationMode`() {
        assertThat(RustRoomNotificationMode.ALL_MESSAGES.map()).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        assertThat(RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY.map()).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        assertThat(RustRoomNotificationMode.MUTE.map()).isEqualTo(RoomNotificationMode.MUTE)
    }
}
