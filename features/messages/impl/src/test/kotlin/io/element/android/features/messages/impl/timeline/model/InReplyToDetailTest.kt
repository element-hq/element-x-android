/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import org.junit.Test

class InReplyToDetailTest {
    @Test
    fun `map - with a not ready InReplyTo does not work`() {
        assertThat(
            InReplyTo.Pending.map(
                permalinkParser = FakePermalinkParser()
            )
        ).isNull()
        assertThat(
            InReplyTo.NotLoaded(AN_EVENT_ID).map(
                permalinkParser = FakePermalinkParser()
            )
        ).isNull()
        assertThat(
            InReplyTo.Error.map(
                permalinkParser = FakePermalinkParser()
            )
        ).isNull()
    }

    @Test
    fun `map - with something other than a MessageContent has no textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderDisplayName = "senderDisplayName",
            senderAvatarUrl = "senderAvatarUrl",
            content = RoomMembershipContent(
                userId = A_USER_ID,
                change = MembershipChange.INVITED,
            )
        )
        val inReplyToDetails = inReplyTo.map(
            permalinkParser = FakePermalinkParser()
        )
        assertThat(inReplyToDetails).isNotNull()
        assertThat(inReplyToDetails?.textContent).isNull()
    }

    @Test
    fun `map - with a message content tries to use the formatted text if exists for its textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderDisplayName = "senderDisplayName",
            senderAvatarUrl = "senderAvatarUrl",
            content = MessageContent(
                body = "**Hello!**",
                inReplyTo = null,
                isEdited = false,
                isThreaded = false,
                type = TextMessageType(
                    body = "**Hello!**",
                    formatted = FormattedBody(
                        format = MessageFormat.HTML,
                        body = "<p><b>Hello!</b></p>"
                    )
                )
            )
        )
        assertThat(
            inReplyTo.map(
                permalinkParser = FakePermalinkParser()
            )?.textContent
        ).isEqualTo("Hello!")
    }

    @Test
    fun `map - with a message content and no formatted body uses body as fallback for textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderDisplayName = "senderDisplayName",
            senderAvatarUrl = "senderAvatarUrl",
            content = MessageContent(
                body = "**Hello!**",
                inReplyTo = null,
                isEdited = false,
                isThreaded = false,
                type = TextMessageType(
                    body = "**Hello!**",
                    formatted = null,
                )
            )
        )
        assertThat(
            inReplyTo.map(
                permalinkParser = FakePermalinkParser()
            )?.textContent
        ).isEqualTo("**Hello!**")
    }
}
