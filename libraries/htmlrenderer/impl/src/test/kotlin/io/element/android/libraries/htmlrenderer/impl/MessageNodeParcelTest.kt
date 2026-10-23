/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.impl

import android.net.Uri
import android.os.Parcel
import androidx.compose.ui.text.AnnotatedString
import androidx.core.os.ParcelCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.htmlrenderer.api.CodeBlockNode
import io.element.android.libraries.htmlrenderer.api.DocumentNode
import io.element.android.libraries.htmlrenderer.api.HeaderNode
import io.element.android.libraries.htmlrenderer.api.ListNode
import io.element.android.libraries.htmlrenderer.api.MentionNodeContent
import io.element.android.libraries.htmlrenderer.api.ParagraphNode
import io.element.android.libraries.htmlrenderer.api.QuoteNode
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jsoup.Jsoup
import org.junit.Test

class MessageNodeParcelTest : RobolectricTest() {
    @Test
    fun `a parsed document with every node type survives a parcel round trip`() {
        val permalinkParser = FakePermalinkParser { url ->
            when {
                url.contains("@alice") -> PermalinkData.UserLink(UserId("@alice:example.org"))
                url.contains("#room") -> PermalinkData.RoomLink(roomIdOrAlias = RoomAlias("#room:example.org").toRoomIdOrAlias())
                else -> PermalinkData.FallbackLink(Uri.parse(url))
            }
        }
        val html = """
            <h3>Title with <em>emphasis</em></h3>
            <p><strong>Bold</strong> and <code>inline code</code>, see <a href="https://element.io">the site</a>.</p>
            <p>Ping <a href="https://matrix.to/#/@alice:example.org">Alice</a> in <a href="https://matrix.to/#/#room:example.org">the room</a>.</p>
            <pre><code>val answer = 42</code></pre>
            <blockquote>
                <p>Quoted</p>
                <ul><li>Nested item</li></ul>
            </blockquote>
            <ol start="3"><li>Third</li><li>Fourth<ol><li>Nested</li></ol></li></ol>
        """.trimIndent()
        val document = DefaultHtmlMessageParser(permalinkParser).parse(Jsoup.parse(html))

        // Make sure the sample covers every node type, and the mention types the parser produces
        assertThat(document.children.map { it::class }).containsAtLeast(
            HeaderNode::class,
            ParagraphNode::class,
            CodeBlockNode::class,
            QuoteNode::class,
            ListNode::class,
        )
        val mentions = document.children.filterIsInstance<ParagraphNode>().flatMap { it.inlineContent.values }
        assertThat(mentions.map { it::class }).containsExactly(
            MentionNodeContent.User::class,
            MentionNodeContent.Room::class,
        )

        assertThat(document.roundTrip()).isEqualTo(document)
    }

    @Test
    fun `an empty document survives a parcel round trip`() {
        val document = DefaultHtmlMessageParser(FakePermalinkParser()).parse(Jsoup.parse(""))

        assertThat(document.roundTrip()).isEqualTo(document)
    }

    @Test
    fun `every mention type survives a parcel round trip`() {
        val paragraph = ParagraphNode(
            text = AnnotatedString("mentions"),
            inlineContent = persistentMapOf(
                "user" to MentionNodeContent.User(displayText = "@Alice", userId = UserId("@alice:example.org")),
                "room" to MentionNodeContent.Room(displayText = "#room", roomIdOrAlias = RoomAlias("#room:example.org").toRoomIdOrAlias()),
                "everyone" to MentionNodeContent.Everyone(displayText = "@room"),
            ),
        )
        val document = DocumentNode(persistentListOf(paragraph))

        val result = document.roundTrip()

        assertThat(result).isEqualTo(document)
        // The iteration order of the mentions is kept too
        assertThat((result?.children?.single() as ParagraphNode).inlineContent.keys).containsExactly("user", "room", "everyone").inOrder()
    }

    private fun DocumentNode.roundTrip(): DocumentNode? {
        val parcel = Parcel.obtain()
        try {
            parcel.writeParcelable(this, 0)
            parcel.setDataPosition(0)
            return ParcelCompat.readParcelable(parcel, DocumentNode::class.java.classLoader, DocumentNode::class.java)
        } finally {
            parcel.recycle()
        }
    }
}
