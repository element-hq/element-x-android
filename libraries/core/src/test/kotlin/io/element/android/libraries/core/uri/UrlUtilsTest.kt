/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.uri

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UrlUtilsTest {
    @Test
    fun `ensureProtocol keeps an empty string unchanged`() {
        assertThat("".ensureProtocol()).isEqualTo("")
    }

    @Test
    fun `ensureProtocol prepends https when there is no scheme`() {
        assertThat("matrix.org".ensureProtocol()).isEqualTo("https://matrix.org")
    }

    @Test
    fun `ensureProtocol keeps an existing http or https scheme`() {
        assertThat("https://matrix.org".ensureProtocol()).isEqualTo("https://matrix.org")
        assertThat("http://matrix.org".ensureProtocol()).isEqualTo("http://matrix.org")
    }

    @Test
    fun `ensureProtocol keeps an uppercase scheme rather than double-prepending`() {
        assertThat("HTTPS://matrix.org".ensureProtocol()).isEqualTo("HTTPS://matrix.org")
        assertThat("Http://matrix.org".ensureProtocol()).isEqualTo("Http://matrix.org")
    }
}
