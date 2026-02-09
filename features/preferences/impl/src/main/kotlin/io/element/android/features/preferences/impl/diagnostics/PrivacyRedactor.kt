/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import dev.zacsweers.metro.Inject

@Inject
class PrivacyRedactor {
    fun redact(input: String): String {
        var result = input
        result = URL_REGEX.replace(result, REDACTED_URL)
        result = EMAIL_REGEX.replace(result, REDACTED_EMAIL)
        result = MATRIX_ID_REGEX.replace(result, REDACTED_MATRIX_ID)
        return result
    }

    companion object {
        const val REDACTED_URL = "<redacted-url>"
        const val REDACTED_EMAIL = "<redacted-email>"
        const val REDACTED_MATRIX_ID = "<redacted-matrix-id>"

        private val URL_REGEX = Regex("""\bhttps?://[^\s]+""", RegexOption.IGNORE_CASE)
        private val EMAIL_REGEX = Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""")
        private val MATRIX_ID_REGEX = Regex("""(?<!\w)[@!#$][^\s:]+:[^\s]+""")
    }
}
