/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrivacyRedactorTest {
    @Test
    fun `redact - replaces url email and matrix ids`() {
        val input = "Contact me at user@example.com, see https://example.com and @alice:matrix.org in !room:example.org"

        val result = PrivacyRedactor().redact(input)

        assertThat(result).contains(PrivacyRedactor.REDACTED_EMAIL)
        assertThat(result).contains(PrivacyRedactor.REDACTED_URL)
        assertThat(result).contains(PrivacyRedactor.REDACTED_MATRIX_ID)
        assertThat(result).doesNotContain("user@example.com")
        assertThat(result).doesNotContain("https://example.com")
        assertThat(result).doesNotContain("@alice:matrix.org")
        assertThat(result).doesNotContain("!room:example.org")
        assertThat(result).contains("Contact me at")
    }
}
