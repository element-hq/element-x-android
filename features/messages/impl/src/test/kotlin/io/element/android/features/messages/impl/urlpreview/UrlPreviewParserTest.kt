/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.jsoup.Jsoup
import org.junit.Test

class UrlPreviewParserTest : RobolectricTest() {
    // Resolves matrix.to links to a Matrix identifier (like the real parser) and treats
    // everything else as a plain, previewable web link.
    private val permalinkParser = FakePermalinkParser { url ->
        when {
            url.contains("matrix.to/#/@") -> PermalinkData.UserLink(UserId("@alice:example.org"))
            url.contains("matrix.to/#/$") -> PermalinkData.RoomLink(
                roomIdOrAlias = RoomId("!room:example.org").toRoomIdOrAlias(),
                eventId = EventId("\$anEventId"),
            )
            url.contains("matrix.to/#/!") -> PermalinkData.RoomLink(
                roomIdOrAlias = RoomId("!room:example.org").toRoomIdOrAlias(),
            )
            else -> PermalinkData.FallbackLink(Uri.parse(url))
        }
    }

    @Test
    fun `find first previewable url returns first previewable raw text url`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "Mail me at jane@example.org or visit https://example.org/first then https://example.org/second",
            htmlDocument = null,
            permalinkParser = permalinkParser,
        )

        assertThat(result).isEqualTo("https://example.org/first")
    }

    @Test
    fun `find first previewable url falls back to html links`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "No spans here",
            htmlDocument = Jsoup.parseBodyFragment("""<a href="https://example.org/path">example</a>"""),
            permalinkParser = permalinkParser,
        )

        assertThat(result).isEqualTo("https://example.org/path")
    }

    @Test
    fun `find first previewable url returns null when no urls found`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "No URLs here at all",
            htmlDocument = null,
            permalinkParser = permalinkParser,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `find first previewable url skips matrix permalinks and returns the first real link`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "Ping https://matrix.to/#/@alice:example.org then read https://example.org/first",
            htmlDocument = null,
            permalinkParser = permalinkParser,
        )

        assertThat(result).isEqualTo("https://example.org/first")
    }

    @Test
    fun `isPreviewableUrl returns true for https`() {
        assertThat(isPreviewableUrl("https://example.org", permalinkParser)).isTrue()
    }

    @Test
    fun `isPreviewableUrl returns true for http`() {
        assertThat(isPreviewableUrl("http://example.org", permalinkParser)).isTrue()
    }

    @Test
    fun `isPreviewableUrl returns false for ftp`() {
        assertThat(isPreviewableUrl("ftp://example.org", permalinkParser)).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for mailto`() {
        assertThat(isPreviewableUrl("mailto:user@example.org", permalinkParser)).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for malformed url`() {
        assertThat(isPreviewableUrl("not a url", permalinkParser)).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for a matrix user mention`() {
        assertThat(isPreviewableUrl("https://matrix.to/#/@alice:example.org", permalinkParser)).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for a matrix room link`() {
        assertThat(isPreviewableUrl("https://matrix.to/#/!room:example.org", permalinkParser)).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for a matrix event permalink`() {
        assertThat(isPreviewableUrl("https://matrix.to/#/\$anEventId", permalinkParser)).isFalse()
    }

    @Test
    fun `hostNameFromUrl extracts hostname`() {
        assertThat(hostNameFromUrl("https://example.org/path")).isEqualTo("example.org")
    }

    @Test
    fun `hostNameFromUrl removes www prefix`() {
        assertThat(hostNameFromUrl("https://www.example.org/path")).isEqualTo("example.org")
    }

    @Test
    fun `hostNameFromUrl falls back to original url for invalid input`() {
        val input = "not a valid url"
        assertThat(hostNameFromUrl(input)).isEqualTo(input)
    }
}
