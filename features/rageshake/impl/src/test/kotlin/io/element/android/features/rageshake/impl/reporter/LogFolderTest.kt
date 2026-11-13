/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class LogFolderTest {
    private val defaultUrl = "https://rageshake.default.org/bugreports/submit".toHttpUrl()
    private val customUrl = "https://rageshakes.example.org/api/submit".toHttpUrl()
    private val otherCustomUrl = "https://rageshakes.other.org/api/submit".toHttpUrl()

    @Test
    fun `default url gives the default folder`() {
        assertThat(computeLogFolder(listOf(defaultUrl), defaultUrl)).isEqualTo(LogFolder.Default)
        assertThat(computeLogFolder(listOf(defaultUrl, defaultUrl), defaultUrl)).isEqualTo(LogFolder.Default)
    }

    @Test
    fun `same custom url gives a server folder`() {
        val expected = LogFolder.Server("rageshakes.example.org_api_submit")
        assertThat(computeLogFolder(listOf(customUrl), defaultUrl)).isEqualTo(expected)
        assertThat(computeLogFolder(listOf(customUrl, customUrl), defaultUrl)).isEqualTo(expected)
    }

    @Test
    fun `disabled bug reports give the local-only folder`() {
        assertThat(computeLogFolder(listOf(null), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
        assertThat(computeLogFolder(listOf(null, null), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
    }

    @Test
    fun `mixed configurations give the local-only folder`() {
        assertThat(computeLogFolder(listOf(customUrl, defaultUrl), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
        assertThat(computeLogFolder(listOf(customUrl, otherCustomUrl), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
        assertThat(computeLogFolder(listOf(defaultUrl, null), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
        assertThat(computeLogFolder(listOf(customUrl, null), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
    }

    @Test
    fun `no url gives the local-only folder`() {
        assertThat(computeLogFolder(emptyList(), defaultUrl)).isEqualTo(LogFolder.LocalOnly)
    }

    @Test
    fun `without default url, disabled bug reports give the default folder`() {
        assertThat(computeLogFolder(listOf(null), null)).isEqualTo(LogFolder.Default)
        assertThat(computeLogFolder(listOf(customUrl), null)).isEqualTo(LogFolder.Server("rageshakes.example.org_api_submit"))
        assertThat(computeLogFolder(listOf(customUrl, null), null)).isEqualTo(LogFolder.LocalOnly)
    }

    @Test
    fun `subfolder names`() {
        assertThat(LogFolder.Default.subfolderName()).isNull()
        assertThat(LogFolder.LocalOnly.subfolderName()).isEqualTo("local-only")
        assertThat(LogFolder.Server("a.org").subfolderName()).isEqualTo("a.org")
    }

    @Test
    fun `url sanitisation`() {
        assertThat("https://rageshakes.example.org/api/submit".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org_api_submit")
        assertThat("https://rageshakes.example.org/api/submit/".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org_api_submit")
        assertThat("https://rageshakes.example.org".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org")
        assertThat("https://rageshakes.example.org:8443/submit".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org_8443_submit")
        assertThat("http://rageshakes.example.org:80/submit".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org_submit")
        assertThat("https://rageshakes.example.org/a%20b/c~d?query=1".toHttpUrl().toFolderName()).isEqualTo("rageshakes.example.org_a_20b_c_d")
    }
}
