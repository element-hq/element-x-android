/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.preferences.api.store.RoomListActivityVisibility
import org.junit.Test

class RoomListRoomSummaryActivityTest {
    @Test
    fun `a room whose unread content notifies gets a badge`() {
        val room = aRoomListRoomSummary(numberOfUnreadNotifications = 1)
        assertThat(room.showsUnreadBadge(RoomListActivityVisibility.CURRENT)).isTrue()
    }

    @Test
    fun `a muted room with unread messages gets no badge`() {
        val room = aRoomListRoomSummary(
            numberOfUnreadMessages = 3,
            notificationMode = RoomNotificationMode.MUTE,
        )
        assertThat(room.showsUnreadBadge(RoomListActivityVisibility.CURRENT)).isFalse()
    }

    @Test
    fun `a mentions-only room with unread messages but no mention gets no badge`() {
        val room = aRoomListRoomSummary(
            numberOfUnreadMessages = 3,
            notificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
        )
        assertThat(room.showsUnreadBadge(RoomListActivityVisibility.CURRENT)).isFalse()
    }

    @Test
    fun `no room gets a badge once badges are turned off`() {
        val room = aRoomListRoomSummary(numberOfUnreadNotifications = 1)
        assertThat(room.showsUnreadBadge(RoomListActivityVisibility.SHOW)).isFalse()
        assertThat(room.showsUnreadBadge(RoomListActivityVisibility.HIDE)).isFalse()
    }

    @Test
    fun `unread content is emphasised unless the activity is hidden`() {
        val room = aRoomListRoomSummary(numberOfUnreadMessages = 3)
        assertThat(room.emphasisesUnreadContent(RoomListActivityVisibility.CURRENT)).isTrue()
        assertThat(room.emphasisesUnreadContent(RoomListActivityVisibility.SHOW)).isTrue()
        assertThat(room.emphasisesUnreadContent(RoomListActivityVisibility.HIDE)).isFalse()
    }

    @Test
    fun `a room with nothing unread is never emphasised`() {
        val room = aRoomListRoomSummary()
        for (visibility in RoomListActivityVisibility.entries) {
            assertThat(room.emphasisesUnreadContent(visibility)).isFalse()
        }
    }
}
