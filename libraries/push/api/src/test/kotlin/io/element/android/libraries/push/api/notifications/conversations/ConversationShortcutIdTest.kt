/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications.conversations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import org.junit.Test

class ConversationShortcutIdTest {
    @Test
    fun `roomId - resolves the room an id was built for, dashes in the domains included`() {
        val sessionId = SessionId("@alice:my-server.example.org")
        val roomId = RoomId("!a-room:other-server.example.org")

        val result = conversationShortcutRoomId(createConversationShortcutId(sessionId, roomId), sessionId)

        assertThat(result).isEqualTo(roomId)
    }

    @Test
    fun `roomId - returns null for another session, or for an id the app did not build`() {
        val sessionId = SessionId("@alice:example.org")

        assertThat(conversationShortcutRoomId("", sessionId)).isNull()
        assertThat(conversationShortcutRoomId("@bob:example.org-!aRoom:example.org", sessionId)).isNull()
        assertThat(conversationShortcutRoomId("@alice:example.org.uk-!aRoom:example.org", sessionId)).isNull()
        assertThat(conversationShortcutRoomId("@alice:example.org-aRoom:example.org", sessionId)).isNull()
    }
}
