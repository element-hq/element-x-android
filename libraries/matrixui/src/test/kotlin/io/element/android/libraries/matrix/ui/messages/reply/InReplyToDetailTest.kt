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

package io.element.android.libraries.matrix.ui.messages.reply

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
import io.element.android.libraries.matrix.test.timeline.aProfileTimelineDetails
import org.junit.Test

class InReplyToDetailTest {
    @Test
    fun `map - with a not ready InReplyTo return expected object`() {
        assertThat(
            InReplyTo.Pending(AN_EVENT_ID).map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Loading(AN_EVENT_ID))
        assertThat(
            InReplyTo.NotLoaded(AN_EVENT_ID).map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Loading(AN_EVENT_ID))
        assertThat(
            InReplyTo.Error(AN_EVENT_ID, "a message").map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Error(AN_EVENT_ID, "a message"))
    }

    @Test
    fun `map - with something other than a MessageContent has no textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileTimelineDetails(),
            content = RoomMembershipContent(
                userId = A_USER_ID,
                userDisplayName = null,
                change = MembershipChange.INVITED,
            )
        )
        val inReplyToDetails = inReplyTo.map(
            permalinkParser = FakePermalinkParser()
        )
        assertThat(inReplyToDetails).isNotNull()
        assertThat((inReplyToDetails as InReplyToDetails.Ready).textContent).isNull()
    }

    @Test
    fun `map - with a message content tries to use the formatted text if exists for its textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileTimelineDetails(),
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
            (inReplyTo.map(permalinkParser = FakePermalinkParser()) as InReplyToDetails.Ready).textContent
        ).isEqualTo("Hello!")
    }

    @Test
    fun `map - with a message content and no formatted body uses body as fallback for textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileTimelineDetails(),
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
            (inReplyTo.map(permalinkParser = FakePermalinkParser()) as InReplyToDetails.Ready).textContent
        ).isEqualTo("**Hello!**")
    }
}
