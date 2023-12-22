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

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
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

    private val myUserColor = Color.RED
    private val otherColor = Color.BLUE
    private val currentUserId = A_SESSION_ID

    private val mentionSpanProvider = MentionSpanProvider(
        currentSessionId = currentUserId,
        currentUserBackgroundColor = myUserColor,
        currentUserTextColor = myUserColor,
        otherBackgroundColor = otherColor,
        otherTextColor = otherColor,
    )

    @Test
    fun `getting mention span for current user should return a MentionSpan with custom colors`() {
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@me:matrix.org", "https://matrix.to/#/${currentUserId.value}")
        assertThat(mentionSpan.backgroundColor).isEqualTo(myUserColor)
        assertThat(mentionSpan.textColor).isEqualTo(myUserColor)
    }

    @Test
    fun `getting mention span for other user should return a MentionSpan with normal colors`() {
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@other:matrix.org", "https://matrix.to/#/@other:matrix.org")
        assertThat(mentionSpan.backgroundColor).isEqualTo(otherColor)
        assertThat(mentionSpan.textColor).isEqualTo(otherColor)
    }

    @Test
    fun `getting mention span for a room should return a MentionSpan with normal colors`() {
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("#room:matrix.org", "https://matrix.to/#/#room:matrix.org")
        assertThat(mentionSpan.backgroundColor).isEqualTo(otherColor)
        assertThat(mentionSpan.textColor).isEqualTo(otherColor)
    }

    @Test
    fun `getting mention span for @room should return a MentionSpan with normal colors`() {
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@room", "#")
        assertThat(mentionSpan.backgroundColor).isEqualTo(otherColor)
        assertThat(mentionSpan.textColor).isEqualTo(otherColor)
    }
}
