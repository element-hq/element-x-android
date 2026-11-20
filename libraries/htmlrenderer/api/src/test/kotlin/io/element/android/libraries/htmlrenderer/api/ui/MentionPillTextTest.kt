/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import org.junit.Test

class MentionPillTextTest {
    private val userMention = MentionNodeContent.User(
        displayText = "@alice:example.org",
        userId = UserId("@alice:example.org"),
        permalinkUrl = "https://matrix.to/#/@alice:example.org",
    )
    private val roomMention = MentionNodeContent.Room(
        displayText = "#room:example.org",
        roomIdOrAlias = RoomAlias("#room:example.org").toRoomIdOrAlias(),
        permalinkUrl = "https://matrix.to/#/#room:example.org",
    )

    @Test
    fun `short display texts are kept as is`() {
        assertThat(userMention.pillText("Alice")).isEqualTo("Alice")
        assertThat(roomMention.pillText("#room:example.org")).isEqualTo("#room:example.org")
    }

    @Test
    fun `a display text at the max length is not truncated`() {
        val text = "a".repeat(MAX_MENTION_PILL_TEXT_LENGTH)
        assertThat(userMention.pillText(text)).isEqualTo(text)
    }

    @Test
    fun `long display texts are truncated with an ellipsis`() {
        val text = "a".repeat(MAX_MENTION_PILL_TEXT_LENGTH + 5)
        assertThat(userMention.pillText(text)).isEqualTo("a".repeat(MAX_MENTION_PILL_TEXT_LENGTH) + "…")
    }

    @Test
    fun `truncation does not split a surrogate pair`() {
        // The emoji takes 2 chars and would be split by the truncation at the max length
        val text = "a".repeat(MAX_MENTION_PILL_TEXT_LENGTH - 1) + "😀" + "bbb"
        assertThat(userMention.pillText(text)).isEqualTo("a".repeat(MAX_MENTION_PILL_TEXT_LENGTH - 1) + "…")
    }

    @Test
    fun `a room mention linking to an event has a prefix`() {
        val eventMention = roomMention.copy(eventId = EventId("\$event"))
        assertThat(eventMention.pillText("#room:example.org")).isEqualTo("💬 > #room:example.org")
    }

    @Test
    fun `the prefix is not counted in the truncation`() {
        val eventMention = roomMention.copy(eventId = EventId("\$event"))
        val text = "a".repeat(MAX_MENTION_PILL_TEXT_LENGTH + 1)
        assertThat(eventMention.pillText(text)).isEqualTo("💬 > " + "a".repeat(MAX_MENTION_PILL_TEXT_LENGTH) + "…")
    }
}
