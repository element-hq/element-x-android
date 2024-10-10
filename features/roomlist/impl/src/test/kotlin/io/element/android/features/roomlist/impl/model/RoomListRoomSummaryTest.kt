/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.test.A_FORMATTED_DATE
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test

class RoomListRoomSummaryTest {
    @Test
    fun `test default value`() {
        val sut = createRoomListRoomSummary(
            isMarkedUnread = false,
        )
        assertThat(sut.isHighlighted).isFalse()
        assertThat(sut.hasNewContent).isFalse()
    }

    @Test
    fun `test muted room`() {
        val sut = createRoomListRoomSummary(
            userDefinedNotificationMode = RoomNotificationMode.MUTE,
        )
        assertThat(sut.isHighlighted).isFalse()
        assertThat(sut.hasNewContent).isFalse()
    }

    @Test
    fun `test muted room isMarkedUnread set to true`() {
        val sut = createRoomListRoomSummary(
            isMarkedUnread = true,
            userDefinedNotificationMode = RoomNotificationMode.MUTE,
        )
        assertThat(sut.isHighlighted).isTrue()
        assertThat(sut.hasNewContent).isTrue()
    }

    @Test
    fun `test muted room with unread message`() {
        val sut = createRoomListRoomSummary(
            numberOfUnreadNotifications = 1,
            userDefinedNotificationMode = RoomNotificationMode.MUTE,
        )
        assertThat(sut.isHighlighted).isFalse()
        assertThat(sut.hasNewContent).isTrue()
    }

    @Test
    fun `test isMarkedUnread set to true`() {
        val sut = createRoomListRoomSummary(
            isMarkedUnread = true,
        )
        assertThat(sut.isHighlighted).isTrue()
        assertThat(sut.hasNewContent).isTrue()
    }

    @Test
    fun `when display type is invite then isHighlighted and hasNewContent are true`() {
        val sut = createRoomListRoomSummary(
            displayType = RoomSummaryDisplayType.INVITE,
        )
        assertThat(sut.isHighlighted).isTrue()
        assertThat(sut.hasNewContent).isTrue()
    }
}

internal fun createRoomListRoomSummary(
    numberOfUnreadMentions: Long = 0,
    numberOfUnreadMessages: Long = 0,
    numberOfUnreadNotifications: Long = 0,
    isMarkedUnread: Boolean = false,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    isFavorite: Boolean = false,
    displayType: RoomSummaryDisplayType = RoomSummaryDisplayType.ROOM,
    heroes: List<AvatarData> = emptyList(),
) = RoomListRoomSummary(
    id = A_ROOM_ID.value,
    roomId = A_ROOM_ID,
    name = A_ROOM_NAME,
    numberOfUnreadMentions = numberOfUnreadMentions,
    numberOfUnreadMessages = numberOfUnreadMessages,
    numberOfUnreadNotifications = numberOfUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    timestamp = A_FORMATTED_DATE,
    lastMessage = "",
    avatarData = AvatarData(id = A_ROOM_ID.value, name = A_ROOM_NAME, size = AvatarSize.RoomListItem),
    displayType = displayType,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = false,
    isDirect = false,
    isFavorite = isFavorite,
    canonicalAlias = null,
    inviteSender = null,
    isDm = false,
    heroes = heroes.toPersistentList(),
)
