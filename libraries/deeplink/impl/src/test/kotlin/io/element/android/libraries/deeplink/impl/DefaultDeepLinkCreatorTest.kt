/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import org.junit.Test

class DefaultDeepLinkCreatorTest {
    @Test
    fun create() {
        val sut = DefaultDeepLinkCreator()
        val sessionId = A_SESSION_ID
        val roomId = A_ROOM_ID
        val threadId = A_THREAD_ID
        val eventId = AN_EVENT_ID
        assertThat(sut.create(sessionId, null, null, null))
            .isEqualTo("elementx://open/%40alice%3Aserver.org")
        assertThat(sut.create(sessionId, roomId, null, null))
            .isEqualTo("elementx://open/%40alice%3Aserver.org/%21aRoomId%3Adomain")
        assertThat(sut.create(sessionId, roomId, threadId, null))
            .isEqualTo("elementx://open/%40alice%3Aserver.org/%21aRoomId%3Adomain/%24aThreadId")
        assertThat(sut.create(sessionId, roomId, threadId, eventId))
            .isEqualTo("elementx://open/%40alice%3Aserver.org/%21aRoomId%3Adomain/%24aThreadId/%24anEventId")
        assertThat(sut.create(sessionId, roomId, null, eventId))
            .isEqualTo("elementx://open/%40alice%3Aserver.org/%21aRoomId%3Adomain//%24anEventId")
    }

    @Test
    fun `create - with escaped invalid characters`() {
        val sut = DefaultDeepLinkCreator()
        val sessionId = SessionId("@a/:domain")
        val roomId = RoomId("!a/RoomId:domain")
        val threadId = ThreadId("\$a/ThreadId")
        val eventId = EventId("\$an/EventId")
        assertThat(sut.create(sessionId, roomId, null, null))
            .isEqualTo("elementx://open/%40a%2F%3Adomain/%21a%2FRoomId%3Adomain")
        assertThat(sut.create(sessionId, roomId, threadId, null))
            .isEqualTo("elementx://open/%40a%2F%3Adomain/%21a%2FRoomId%3Adomain/%24a%2FThreadId")
        assertThat(sut.create(sessionId, roomId, threadId, eventId))
            .isEqualTo("elementx://open/%40a%2F%3Adomain/%21a%2FRoomId%3Adomain/%24a%2FThreadId/%24an%2FEventId")
        assertThat(sut.create(sessionId, roomId, null, eventId))
            .isEqualTo("elementx://open/%40a%2F%3Adomain/%21a%2FRoomId%3Adomain//%24an%2FEventId")
    }
}
