/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import org.junit.Test

class NotificationCreatorTest {
    @Test
    fun `getMessageNotificationRoomId returns the room id of a room message tag`() {
        val tag = NotificationCreator.messageTag(A_ROOM_ID, null)
        assertThat(tag).isEqualTo(A_ROOM_ID.value)
        assertThat(NotificationCreator.getMessageNotificationRoomId(tag)).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `getMessageNotificationRoomId returns the room id of a thread message tag`() {
        val tag = NotificationCreator.messageTag(A_ROOM_ID, A_THREAD_ID)
        assertThat(tag).isEqualTo("${A_ROOM_ID.value}|${A_THREAD_ID.value}")
        assertThat(NotificationCreator.getMessageNotificationRoomId(tag)).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `getMessageNotificationRoomId returns null for a tag which is not a room id`() {
        assertThat(NotificationCreator.getMessageNotificationRoomId(AN_EVENT_ID.value)).isNull()
        assertThat(NotificationCreator.getMessageNotificationRoomId("")).isNull()
    }
}
