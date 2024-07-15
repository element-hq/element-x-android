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

package io.element.android.libraries.textcomposer.impl.model

import android.net.Uri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownTextEditorStateTest {
    @Test
    fun `insertMention - with no currentMentionSuggestion does nothing`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true)
        val member = aRoomMember()
        val mention = ResolvedMentionSuggestion.Member(member)
        val permalinkBuilder = FakePermalinkBuilder()
        val mentionSpanProvider = aMentionSpanProvider()

        state.insertMention(mention, mentionSpanProvider, permalinkBuilder)

        assertThat(state.getMentions()).isEmpty()
    }

    @Test
    fun `insertMention - with member but failed PermalinkBuilder result`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true).apply {
            currentMentionSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Mention, text = "")
        }
        val member = aRoomMember()
        val mention = ResolvedMentionSuggestion.Member(member)
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.UserLink(member.userId) })
        val permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = { Result.failure(IllegalStateException("Failed")) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)

        state.insertMention(mention, mentionSpanProvider, permalinkBuilder)

        val mentions = state.getMentions()
        assertThat(mentions).isEmpty()
    }

    @Test
    fun `insertMention - with member`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true).apply {
            currentMentionSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Mention, text = "")
        }
        val member = aRoomMember()
        val mention = ResolvedMentionSuggestion.Member(member)
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.UserLink(member.userId) })
        val permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = { Result.success("https://matrix.to/#/${member.userId}") })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)

        state.insertMention(mention, mentionSpanProvider, permalinkBuilder)

        val mentions = state.getMentions()
        assertThat(mentions).isNotEmpty()
        assertThat((mentions.firstOrNull() as? Mention.User)?.userId).isEqualTo(member.userId)
    }

    @Test
    fun `insertMention - with @room`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true).apply {
            currentMentionSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Mention, text = "")
        }
        val mention = ResolvedMentionSuggestion.AtRoom
        val permalinkBuilder = FakePermalinkBuilder()
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.FallbackLink(Uri.EMPTY, false) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)

        state.insertMention(mention, mentionSpanProvider, permalinkBuilder)

        val mentions = state.getMentions()
        assertThat(mentions).isNotEmpty()
        assertThat(mentions.firstOrNull()).isInstanceOf(Mention.AtRoom::class.java)
    }

    @Test
    fun `getMessageMarkdown - when there are no MentionSpans returns the same text`() {
        val text = "No mentions here"
        val state = MarkdownTextEditorState(initialText = text, initialFocus = true)

        val markdown = state.getMessageMarkdown(FakePermalinkBuilder())

        assertThat(markdown).isEqualTo(text)
    }

    @Test
    fun `getMessageMarkdown - when there are MentionSpans returns the same text with links to the mentions`() {
        val text = "No mentions here"
        val permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = { Result.success("https://matrix.to/#/$it") })
        val state = MarkdownTextEditorState(initialText = text, initialFocus = true)
        state.text.update(aMarkdownTextWithMentions(), needsDisplaying = false)

        val markdown = state.getMessageMarkdown(permalinkBuilder = permalinkBuilder)

        assertThat(markdown).isEqualTo(
            "Hello [@alice:matrix.org](https://matrix.to/#/@alice:matrix.org) and everyone in @room"
        )
    }

    @Test
    fun `getMentions - when there are no MentionSpans returns empty list of mentions`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true)

        assertThat(state.getMentions()).isEmpty()
    }

    @Test
    fun `getMentions - when there are MentionSpans returns a list of mentions`() {
        val state = MarkdownTextEditorState(initialText = "Hello @", initialFocus = true)
        state.text.update(aMarkdownTextWithMentions(), needsDisplaying = false)

        val mentions = state.getMentions()

        assertThat(mentions).isNotEmpty()
        assertThat((mentions.firstOrNull() as? Mention.User)?.userId?.value).isEqualTo("@alice:matrix.org")
        assertThat(mentions.lastOrNull()).isInstanceOf(Mention.AtRoom::class.java)
    }

    private fun aMentionSpanProvider(
        permalinkParser: FakePermalinkParser = FakePermalinkParser(),
    ): MentionSpanProvider {
        return MentionSpanProvider(permalinkParser)
    }

    private fun aMarkdownTextWithMentions(): CharSequence {
        val userMentionSpan = MentionSpan("@Alice", "@alice:matrix.org", MentionSpan.Type.USER)
        val atRoomMentionSpan = MentionSpan("@room", "@room", MentionSpan.Type.EVERYONE)
        return buildSpannedString {
            append("Hello ")
            inSpans(userMentionSpan) {
                append("@")
            }
            append(" and everyone in ")
            inSpans(atRoomMentionSpan) {
                append("@")
            }
        }
    }
}
