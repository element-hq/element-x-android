/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ToHtmlDocumentTest : RobolectricTest() {
    @Test
    fun `toHtmlDocument - returns null if format is not HTML`() {
        val body = FormattedBody(
            format = MessageFormat.UNKNOWN,
            body = "Hello world"
        )

        val document = body.toHtmlDocument(permalinkParser = FakePermalinkParser())

        assertThat(document).isNull()
    }

    @Test
    fun `toHtmlDocument - returns a Document if the format is HTML`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<p>Hello world</p>"
        )

        val document = body.toHtmlDocument(permalinkParser = FakePermalinkParser())
        assertThat(document).isNotNull()
        assertThat(document?.text()).isEqualTo("Hello world")
    }

    @Test
    fun `toHtmlDocument - renders heading tags as bold paragraphs`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<h4>Title</h4>submitted by someone"
        )

        val document = body.toHtmlDocument(permalinkParser = FakePermalinkParser())

        assertThat(document?.select("h1, h2, h3, h4, h5, h6")).isEmpty()
        assertThat(document?.body()?.html()).contains("<p><b>Title</b></p>")
        assertThat(document?.text()).isEqualTo("Title submitted by someone")
    }

    @Test
    fun `toHtmlDocument - keeps the text of every heading level`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<h1>One</h1><h2>Two</h2><h3>Three</h3><h4>Four</h4><h5>Five</h5><h6>Six</h6>"
        )

        val document = body.toHtmlDocument(permalinkParser = FakePermalinkParser())

        assertThat(document?.select("h1, h2, h3, h4, h5, h6")).isEmpty()
        assertThat(document?.select("p")).hasSize(6)
        assertThat(document?.text()).isEqualTo("One Two Three Four Five Six")
    }

    @Test
    fun `toHtmlDocument - returns a Document with a prefix if provided`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "<p>Hello world</p>"
        )

        val document = body.toHtmlDocument(
            permalinkParser = FakePermalinkParser(),
            prefix = "@Jorge:"
        )
        assertThat(document).isNotNull()
        assertThat(document?.text()).isEqualTo("@Jorge: Hello world")
    }

    @Test
    fun `toHtmlDocument - if a mention is found without an '@' prefix, it will be added`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.to/#/@alice:matrix.org'>Alice</a>!"
        )

        val document = body.toHtmlDocument(permalinkParser = object : PermalinkParser {
            override fun parse(uriString: String): PermalinkData {
                return PermalinkData.UserLink(UserId("@alice:matrix.org"))
            }
        })
        assertThat(document?.text()).isEqualTo("Hey @Alice!")
    }

    @Test
    fun `toHtmlDocument - if a mention is found with an '@' prefix, nothing will be done`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.to/#/@alice:matrix.org'>@Alice</a>!"
        )

        val document = body.toHtmlDocument(permalinkParser = object : PermalinkParser {
            override fun parse(uriString: String): PermalinkData {
                return PermalinkData.UserLink(UserId("@alice:matrix.org"))
            }
        })
        assertThat(document?.text()).isEqualTo("Hey @Alice!")
    }

    @Test
    fun `toHtmlDocument - if a link is not a mention, nothing will be done for it`() {
        val body = FormattedBody(
            format = MessageFormat.HTML,
            body = "Hey <a href='https://matrix.org'>Alice</a>!"
        )

        val document = body.toHtmlDocument(permalinkParser = object : PermalinkParser {
            override fun parse(uriString: String): PermalinkData {
                return PermalinkData.FallbackLink(uri = Uri.parse("https://matrix.org"))
            }
        })
        assertThat(document?.text()).isEqualTo("Hey Alice!")
    }
}
