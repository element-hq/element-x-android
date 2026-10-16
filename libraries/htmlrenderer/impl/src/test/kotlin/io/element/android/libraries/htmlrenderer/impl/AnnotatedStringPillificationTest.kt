/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl

import android.net.Uri
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.INLINE_CODE_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.HtmlMessageParser.Companion.LINK_ANNOTATION_TAG
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class AnnotatedStringPillificationTest : RobolectricTest() {
    private val userId = UserId("@alice:example.org")
    private val roomAlias = RoomAlias("#room:example.org")

    @Test
    fun `text without mentions is returned as is`() {
        val text = AnnotatedString("Hello world, visit https://element.io")
        val result = text.pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.text).isEqualTo(text)
        assertThat(result.inlineContent).isEmpty()
    }

    @Test
    fun `a raw user id becomes a user pill`() {
        val result = AnnotatedString("Hi @alice:example.org!").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        val (id, mention) = result.inlineContent.entries.single()
        assertThat(
            mention
        ).isEqualTo(MentionNodeContent.User(displayText = "@alice:example.org", userId = userId, permalinkUrl = "https://matrix.to/#/@alice:example.org"))
        assertThat(result.text.text).isEqualTo("Hi @alice:example.org!")
        result.assertPlaceholder(id, start = 3, end = 21)
    }

    @Test
    fun `a raw room alias becomes a room pill`() {
        val result = AnnotatedString("Join #room:example.org.").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        val (id, mention) = result.inlineContent.entries.single()
        assertThat(
            mention
        ).isEqualTo(
            MentionNodeContent.Room(
                displayText = "#room:example.org",
                roomIdOrAlias = roomAlias.toRoomIdOrAlias(),
                permalinkUrl = "https://matrix.to/#/#room:example.org"
            )
        )
        // The trailing punctuation is not part of the pill
        assertThat(result.text.text).isEqualTo("Join #room:example.org.")
        result.assertPlaceholder(id, start = 5, end = 22)
    }

    @Test
    fun `@room becomes an everyone pill`() {
        val result = AnnotatedString("Hey @room: look").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        val (id, mention) = result.inlineContent.entries.single()
        assertThat(mention).isEqualTo(MentionNodeContent.Everyone(displayText = "@room"))
        result.assertPlaceholder(id, start = 4, end = 9)
    }

    @Test
    fun `@room inside a word is not pillified`() {
        val result = AnnotatedString("My @roomba is at me@room").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.inlineContent).isEmpty()
    }

    @Test
    fun `a user id starting with @room is a single user pill`() {
        val result = AnnotatedString("Hi @room:example.org").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.inlineContent.values.single())
            .isEqualTo(
                MentionNodeContent.User(
                    displayText = "@room:example.org",
                    userId = UserId("@room:example.org"),
                    permalinkUrl = "https://matrix.to/#/@room:example.org"
                )
            )
    }

    @Test
    fun `room ids and event ids are not pillified`() {
        val result = AnnotatedString("See !room:example.org and \$event:example.org").pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.inlineContent).isEmpty()
    }

    @Test
    fun `a user permalink becomes a user pill`() {
        val permalink = "https://matrix.to/#/@alice:example.org"
        val permalinkParser = aPermalinkParser { if (it == permalink) PermalinkData.UserLink(userId) else null }
        val result = AnnotatedString("Ask $permalink.").pillify(emptyMap(), permalinkParser, aPermalinkBuilder())
        val (id, mention) = result.inlineContent.entries.single()
        assertThat(
            mention
        ).isEqualTo(MentionNodeContent.User(displayText = "@alice:example.org", userId = userId, permalinkUrl = "https://matrix.to/#/@alice:example.org"))
        // The url is replaced by the display text of the pill, the trailing punctuation is kept
        assertThat(result.text.text).isEqualTo("Ask @alice:example.org.")
        result.assertPlaceholder(id, start = 4, end = 22)
    }

    @Test
    fun `a matrix uri room permalink becomes a room pill`() {
        val permalink = "matrix:r/room:example.org"
        val permalinkParser = aPermalinkParser { if (it == permalink) PermalinkData.RoomLink(roomAlias.toRoomIdOrAlias()) else null }
        val result = AnnotatedString("Join $permalink").pillify(emptyMap(), permalinkParser, aPermalinkBuilder())
        assertThat(result.inlineContent.values.single())
            .isEqualTo(
                MentionNodeContent.Room(
                    displayText = "#room:example.org",
                    roomIdOrAlias = roomAlias.toRoomIdOrAlias(),
                    permalinkUrl = "https://matrix.to/#/#room:example.org"
                )
            )
        assertThat(result.text.text).isEqualTo("Join #room:example.org")
    }

    @Test
    fun `an event permalink is not pillified`() {
        val permalink = "https://matrix.to/#/!room:example.org/\$event"
        val permalinkParser = aPermalinkParser {
            if (it == permalink) {
                PermalinkData.RoomLink(RoomId("!room:example.org").toRoomIdOrAlias(), eventId = EventId("\$event:example.org"))
            } else {
                null
            }
        }
        val result = AnnotatedString("See $permalink").pillify(emptyMap(), permalinkParser, aPermalinkBuilder())
        assertThat(result.inlineContent).isEmpty()
    }

    @Test
    fun `mentions inside links and inline code are not pillified`() {
        val text = buildAnnotatedString {
            withAnnotation(LINK_ANNOTATION_TAG, "https://element.io") { append("@alice:example.org") }
            append(" and ")
            withAnnotation(INLINE_CODE_ANNOTATION_TAG, "") { append("@room") }
        }
        val result = text.pillify(emptyMap(), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.text).isEqualTo(text)
        assertThat(result.inlineContent).isEmpty()
    }

    @Test
    fun `existing mentions are kept and not pillified again`() {
        val existingMention = MentionNodeContent.User(
            displayText = "@alice:example.org",
            userId = userId,
            permalinkUrl = "https://matrix.to/#/@alice:example.org"
        )
        val text = buildAnnotatedString {
            appendInlineContent("mention_0", existingMention.displayText)
            append(" and @bob:example.org")
        }
        val result = text.pillify(mapOf("mention_0" to existingMention), aPermalinkParser(), aPermalinkBuilder())
        assertThat(result.inlineContent).hasSize(2)
        assertThat(result.inlineContent["mention_0"]).isEqualTo(existingMention)
        val (newId, newMention) = result.inlineContent.entries.single { it.key != "mention_0" }
        assertThat(
            newMention
        ).isEqualTo(
            MentionNodeContent.User(
                displayText = "@bob:example.org",
                userId = UserId("@bob:example.org"),
                permalinkUrl = "https://matrix.to/#/@bob:example.org"
            )
        )
        result.assertPlaceholder("mention_0", start = 0, end = 18)
        result.assertPlaceholder(newId, start = 23, end = 39)
    }

    @Test
    fun `several mentions keep the styles and annotations around them`() {
        val permalink = "https://matrix.to/#/@alice:example.org"
        val permalinkParser = aPermalinkParser { if (it == permalink) PermalinkData.UserLink(userId) else null }
        val text = buildAnnotatedString {
            append("$permalink and @room ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("bold") }
            append(" ")
            withAnnotation(LINK_ANNOTATION_TAG, "https://element.io") { append("link") }
        }
        val result = text.pillify(emptyMap(), permalinkParser, aPermalinkBuilder())
        assertThat(result.text.text).isEqualTo("@alice:example.org and @room bold link")
        assertThat(result.inlineContent.values).containsExactly(
            MentionNodeContent.User(displayText = "@alice:example.org", userId = userId, permalinkUrl = "https://matrix.to/#/@alice:example.org"),
            MentionNodeContent.Everyone(displayText = "@room"),
        )
        val bold = result.text.spanStyles.single()
        assertThat(bold.start).isEqualTo(29)
        assertThat(bold.end).isEqualTo(33)
        val link = result.text.getStringAnnotations(LINK_ANNOTATION_TAG, 0, result.text.length).single()
        assertThat(link.start).isEqualTo(34)
        assertThat(link.end).isEqualTo(38)
    }

    private fun PillifiedText.assertPlaceholder(id: String, start: Int, end: Int) {
        val placeholder = text.getStringAnnotations(0, text.length).single { it.item == id }
        assertThat(placeholder.start).isEqualTo(start)
        assertThat(placeholder.end).isEqualTo(end)
    }

    /** Returns the permalink data given by [block], or a fallback link if it returns `null`. */
    private fun aPermalinkParser(block: (String) -> PermalinkData? = { null }): PermalinkParser = FakePermalinkParser { url ->
        block(url) ?: PermalinkData.FallbackLink(Uri.parse(url))
    }

    private fun aPermalinkBuilder(): PermalinkBuilder = object : PermalinkBuilder {
        override fun permalinkForUser(userId: UserId): Result<String> = Result.success("https://matrix.to/#/${userId.value}")
        override fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String> = Result.success("https://matrix.to/#/${roomAlias.value}")
    }
}
