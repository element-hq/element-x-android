/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import com.google.common.truth.Truth.assertThat
import io.element.android.wysiwyg.link.Link
import org.junit.Test

class DefaultLinkCheckerTest {
    private val sut = DefaultLinkChecker()

    @Test
    fun `when url and text are identical, the link is safe`() {
        assertThat(sut.isSafe(Link("url", "url"))).isTrue()
    }

    @Test
    fun `when url is not safe, the link is safe`() {
        assertThat(sut.isSafe(Link("url", "https://example.org"))).isTrue()
    }

    @Test
    fun `when text is a url, and url is identical the link is safe`() {
        assertThat(sut.isSafe(Link("https://example.org", "https://example.org"))).isTrue()
    }

    @Test
    fun `when url contains RtL char, the link is not safe`() {
        assertThat(sut.isSafe(Link("https://example\u202E.org", "text"))).isFalse()
    }

    @Test
    fun `when text is not a url, the link is safe`() {
        assertThat(sut.isSafe(Link("https://example.org", "url"))).isTrue()
    }

    @Test
    fun `when text is a url and hosts match, the link is safe`() {
        assertThat(sut.isSafe(Link("https://example.org/some/path", "https://example.org"))).isTrue()
    }

    @Test
    fun `when text is a url and hosts do not match, the link is safe`() {
        assertThat(sut.isSafe(Link("https://example.org", "https://evil.org"))).isFalse()
    }
}
