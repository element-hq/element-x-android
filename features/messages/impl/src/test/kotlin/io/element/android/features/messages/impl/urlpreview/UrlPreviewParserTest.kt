/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test

class UrlPreviewParserTest {
    @Test
    fun `find first previewable url returns first previewable raw text url`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "Mail me at jane@example.org or visit https://example.org/first then https://example.org/second",
            htmlDocument = null,
        )

        assertThat(result).isEqualTo("https://example.org/first")
    }

    @Test
    fun `find first previewable url falls back to html links`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "No spans here",
            htmlDocument = Jsoup.parseBodyFragment("""<a href="https://example.org/path">example</a>"""),
        )

        assertThat(result).isEqualTo("https://example.org/path")
    }

    @Test
    fun `find first previewable url returns null when no urls found`() {
        val result = findFirstPreviewableUrl(
            formattedBody = "No URLs here at all",
            htmlDocument = null,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `isPreviewableUrl returns true for https`() {
        assertThat(isPreviewableUrl("https://example.org")).isTrue()
    }

    @Test
    fun `isPreviewableUrl returns true for http`() {
        assertThat(isPreviewableUrl("http://example.org")).isTrue()
    }

    @Test
    fun `isPreviewableUrl returns false for ftp`() {
        assertThat(isPreviewableUrl("ftp://example.org")).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for mailto`() {
        assertThat(isPreviewableUrl("mailto:user@example.org")).isFalse()
    }

    @Test
    fun `isPreviewableUrl returns false for malformed url`() {
        assertThat(isPreviewableUrl("not a url")).isFalse()
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
