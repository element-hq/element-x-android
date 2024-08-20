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

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.ui.messages.LocalRoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import io.element.android.wysiwyg.view.spans.CustomMentionSpan
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineTextViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `getTextWithResolvedMentions - does nothing for a non spannable CharSequence`() = runTest {
        val charSequence = "Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>"

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans()).isEmpty()
    }

    @Test
    fun `getTextWithResolvedMentions - does nothing if there are no mentions`() = runTest {
        val charSequence = SpannableString("Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>")

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans()).isEmpty()
    }

    @Test
    fun `getTextWithResolvedMentions - just returns the body if there is no formattedBody`() = runTest {
        val charSequence = "Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>"

        val result = rule.getText(aTextContentWithFormattedBody(body = charSequence, formattedBody = null))

        assertThat(result.getMentionSpans()).isEmpty()
        assertThat(result.toString()).isEqualTo(charSequence)
    }

    @Test
    fun `getTextWithResolvedMentions - with Room mention does nothing`() = runTest {
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(aMentionSpan(rawValue = A_ROOM_ID_2.value, type = MentionSpan.Type.ROOM)) {
                append(A_ROOM_ID.value)
            }
        }

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans().firstOrNull()?.text).isEmpty()
        assertThat(result).isEqualTo(charSequence)
    }

    @Test
    fun `getTextWithResolvedMentions - replaces MentionSpan's text`() = runTest {
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(aMentionSpan(rawValue = A_USER_ID.value)) {
                append("@NotAlice")
            }
        }

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans().firstOrNull()?.text).isEqualTo("alice")
    }

    @Test
    fun `getTextWithResolvedMentions - replaces MentionSpan's text inside CustomMentionSpan`() = runTest {
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(CustomMentionSpan(aMentionSpan(rawValue = A_USER_ID.value))) {
                append("@NotAlice")
            }
        }

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans().firstOrNull()?.text).isEqualTo("alice")
    }

    @Test
    fun `getTextWithResolvedMentions - replaces MentionSpan's text with user id if no display name is cached`() = runTest {
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(aMentionSpan(rawValue = A_USER_ID_2.value)) {
                append("@NotAlice")
            }
        }

        val result = rule.getText(aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans().firstOrNull()?.text).isEqualTo(A_USER_ID_2.value)
    }

    private suspend fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.getText(
        content: TimelineItemTextBasedContent,
    ): CharSequence {
        val completable = CompletableDeferred<CharSequence>()
        setContent {
            val roomMemberProfilesCache = RoomMemberProfilesCache().apply {
                replace(listOf(aRoomMember(userId = A_USER_ID, displayName = A_USER_NAME)))
            }
            CompositionLocalProvider(
                LocalRoomMemberProfilesCache provides roomMemberProfilesCache,
            ) {
                completable.complete(getTextWithResolvedMentions(content = content))
            }
        }
        return completable.await()
    }

    private fun aMentionSpan(
        rawValue: String,
        text: String = "",
        type: MentionSpan.Type = MentionSpan.Type.USER
    ) = MentionSpan(
        text = text,
        rawValue = rawValue,
        type = type,
    )

    private fun aTextContentWithFormattedBody(formattedBody: CharSequence?, body: String = "") =
        TimelineItemTextContent(
            body = body,
            htmlDocument = null,
            formattedBody = formattedBody,
            isEdited = false
        )
}
