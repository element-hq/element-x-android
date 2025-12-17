/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.impl.model

import android.net.Uri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.textcomposer.impl.mentions.aMentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionType
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.libraries.textcomposer.model.aMarkdownTextEditorState
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownTextEditorStateTest {
    @Test
    fun `insertMention - room alias - getMentions return empty list`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true)
        val suggestion = aRoomAliasSuggestion()
        val mentionSpanProvider = aMentionSpanProvider()
        state.insertSuggestion(suggestion, mentionSpanProvider)
        assertThat(state.getMentions()).isEmpty()
    }

    @Test
    fun `insertSuggestion - room alias - with member but failed PermalinkBuilder result`() {
        val state = aMarkdownTextEditorState(initialText = "Hello #", initialFocus = true).apply {
            currentSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Room, text = "")
        }
        val suggestion = aRoomAliasSuggestion()
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.RoomLink(A_ROOM_ALIAS.toRoomIdOrAlias()) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)
        state.insertSuggestion(suggestion, mentionSpanProvider)
    }

    @Test
    fun `insertSuggestion - room alias`() {
        val state = aMarkdownTextEditorState(initialText = "Hello #", initialFocus = true).apply {
            currentSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Room, text = "")
        }
        val suggestion = aRoomAliasSuggestion()
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.RoomLink(A_ROOM_ALIAS.toRoomIdOrAlias()) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)
        state.insertSuggestion(suggestion, mentionSpanProvider)
    }

    @Test
    fun `insertSuggestion - with no currentMentionSuggestion does nothing`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true)
        val member = aRoomMember()
        val mention = ResolvedSuggestion.Member(member)
        val mentionSpanProvider = aMentionSpanProvider()
        state.insertSuggestion(mention, mentionSpanProvider)
        assertThat(state.getMentions()).isEmpty()
    }

    @Test
    fun `insertSuggestion - with member`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true).apply {
            currentSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Mention, text = "")
        }
        val member = aRoomMember()
        val mention = ResolvedSuggestion.Member(member)
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.UserLink(member.userId) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)

        state.insertSuggestion(mention, mentionSpanProvider)

        val mentions = state.getMentions()
        assertThat(mentions).isNotEmpty()
        assertThat((mentions.firstOrNull() as? IntentionalMention.User)?.userId).isEqualTo(member.userId)
    }

    @Test
    fun `insertSuggestion - with @room`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true).apply {
            currentSuggestion = Suggestion(start = 6, end = 7, type = SuggestionType.Mention, text = "")
        }
        val mention = ResolvedSuggestion.AtRoom
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.FallbackLink(Uri.EMPTY, false) })
        val mentionSpanProvider = aMentionSpanProvider(permalinkParser = permalinkParser)

        state.insertSuggestion(mention, mentionSpanProvider)

        val mentions = state.getMentions()
        assertThat(mentions).isNotEmpty()
        assertThat(mentions.firstOrNull()).isInstanceOf(IntentionalMention.Room::class.java)
    }

    @Test
    fun `getMessageMarkdown - when there are no MentionSpans returns the same text`() {
        val text = "No mentions here"
        val state = aMarkdownTextEditorState(initialText = text, initialFocus = true)

        val markdown = state.getMessageMarkdown(FakePermalinkBuilder())

        assertThat(markdown).isEqualTo(text)
    }

    @Test
    fun `getMessageMarkdown - when there are MentionSpans returns the same text with links to the mentions`() {
        val text = "No mentions here"
        val permalinkBuilder = FakePermalinkBuilder(
            permalinkForUserLambda = { Result.success("https://matrix.to/#/$it") },
            permalinkForRoomAliasLambda = { Result.success("https://matrix.to/#/$it") },
        )
        val state = aMarkdownTextEditorState(initialText = text, initialFocus = true)
        state.text.update(aMarkdownTextWithMentions(), needsDisplaying = false)

        val markdown = state.getMessageMarkdown(permalinkBuilder = permalinkBuilder)

        assertThat(markdown).isEqualTo(
            "Hello [@alice:matrix.org](https://matrix.to/#/@alice:matrix.org) and everyone in @room" +
                " and a room [#room:domain.org](https://matrix.to/#/#room:domain.org)"
        )
    }

    @Test
    fun `getMentions - when there are no MentionSpans returns empty list of mentions`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true)

        assertThat(state.getMentions()).isEmpty()
    }

    @Test
    fun `getMentions - when there are MentionSpans returns a list of mentions`() {
        val state = aMarkdownTextEditorState(initialText = "Hello @", initialFocus = true)
        state.text.update(aMarkdownTextWithMentions(), needsDisplaying = false)

        val mentions = state.getMentions()

        assertThat(mentions).isNotEmpty()
        assertThat((mentions.firstOrNull() as? IntentionalMention.User)?.userId?.value).isEqualTo("@alice:matrix.org")
        assertThat(mentions.lastOrNull()).isInstanceOf(IntentionalMention.Room::class.java)
    }

    private fun aMarkdownTextWithMentions(): CharSequence {
        val userMentionSpan = MentionSpan(MentionType.User(UserId("@alice:matrix.org")))
        val atRoomMentionSpan = MentionSpan(MentionType.Everyone)
        val roomMentionSpan = MentionSpan(MentionType.Room(RoomAlias("#room:domain.org").toRoomIdOrAlias()))
        return buildSpannedString {
            append("Hello ")
            inSpans(userMentionSpan) {
                append("@")
            }
            append(" and everyone in ")
            inSpans(atRoomMentionSpan) {
                append("@")
            }
            append(" and a room ")
            inSpans(roomMentionSpan) {
                append("#room:domain.org")
            }
        }
    }

    private fun aRoomAliasSuggestion(): ResolvedSuggestion.Alias {
        return ResolvedSuggestion.Alias(
            roomAlias = A_ROOM_ALIAS,
            roomId = A_ROOM_ID,
            roomName = null,
            roomAvatarUrl = null
        )
    }
}
