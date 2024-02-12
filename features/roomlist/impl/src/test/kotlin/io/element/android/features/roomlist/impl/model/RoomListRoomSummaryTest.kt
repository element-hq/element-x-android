/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.test.A_FORMATTED_DATE
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
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
}

internal fun createRoomListRoomSummary(
    numberOfUnreadMentions: Int = 0,
    numberOfUnreadMessages: Int = 0,
    numberOfUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    userDefinedNotificationMode: RoomNotificationMode? = null,
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
    isPlaceholder = false,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = false,
    isDm = false,
)
