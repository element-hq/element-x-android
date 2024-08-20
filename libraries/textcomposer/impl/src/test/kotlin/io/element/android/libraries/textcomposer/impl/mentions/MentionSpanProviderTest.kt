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

package io.element.android.libraries.textcomposer.impl.mentions

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.tests.testutils.WarmUpRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MentionSpanProviderTest {
    @JvmField @Rule
    val warmUpRule = WarmUpRule()

    private val permalinkParser = FakePermalinkParser()
    private val mentionSpanProvider = MentionSpanProvider(
        permalinkParser = permalinkParser,
    )

    @Test
    fun `getting mention span for a user returns a MentionSpan of type USER`() {
        permalinkParser.givenResult(PermalinkData.UserLink(A_USER_ID))
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@me:matrix.org", "https://matrix.to/#/${A_USER_ID.value}")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.USER)
    }

    @Test
    fun `getting mention span for everyone in the room returns a MentionSpan of type EVERYONE`() {
        permalinkParser.givenResult(PermalinkData.FallbackLink(uri = Uri.EMPTY))
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@room", "#")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.EVERYONE)
    }

    @Test
    fun `getting mention span for a room returns a MentionSpan of type ROOM`() {
        permalinkParser.givenResult(
            PermalinkData.RoomLink(
                roomIdOrAlias = RoomAlias("#room:matrix.org").toRoomIdOrAlias(),
            )
        )
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("#room:matrix.org", "https://matrix.to/#/#room:matrix.org")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.ROOM)
    }
}
