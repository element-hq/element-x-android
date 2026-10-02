/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.model.createRoomListRoomSummary
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import org.junit.Test

class RoomSummaryRowUnreadIndicatorCountTest {
    @Test
    fun `unread notifications are used when the room is not muted`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 3,
            numberOfUnreadMessages = 5,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = false)).isEqualTo(3L)
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = true)).isEqualTo(3L)
    }

    @Test
    fun `unread notifications only show a dot when showUnreadCount is false`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 3,
            numberOfUnreadMessages = 5,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = false)).isEqualTo(0L)
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = true)).isEqualTo(0L)
    }

    @Test
    fun `unread messages are used when there are no unread notifications and showAllActivity is true, but always display a dot`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 0,
            numberOfUnreadMessages = 5,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = true)).isEqualTo(0L)
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = true)).isEqualTo(0L)
    }

    @Test
    fun `unread messages are ignored when showAllActivity is false`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 0,
            numberOfUnreadMessages = 5,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = false)).isNull()
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = false)).isNull()
    }

    @Test
    fun `muted room ignores unread notifications and falls back to unread messages displaying a dot when showAllActivity is true`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 3,
            numberOfUnreadMessages = 5,
            userDefinedNotificationMode = RoomNotificationMode.MUTE,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = true)).isEqualTo(0L)
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = true)).isEqualTo(0L)
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = false)).isNull()
    }

    @Test
    fun `no count when there are no unread notifications nor unread messages`() {
        val room = createRoomListRoomSummary(
            numberOfUnreadNotifications = 0,
            numberOfUnreadMessages = 0,
            isMarkedUnread = true,
        )
        assertThat(room.unreadIndicatorCount(showUnreadCount = true, showAllActivity = true)).isNull()
        assertThat(room.unreadIndicatorCount(showUnreadCount = false, showAllActivity = false)).isNull()
    }
}
