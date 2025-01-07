/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.hash

import java.security.MessageDigest
import java.util.Locale

/**
 * Compute a Hash of a String, using md5 algorithm.
 */
fun String.md5() = try {
    val digest = MessageDigest.getInstance("md5")
    val locale = Locale.ROOT
    digest.update(toByteArray())
    digest.digest()
        .joinToString("") { String.format(locale, "%02X", it) }
        .lowercase(locale)
} catch (exc: Exception) {
    // Should not happen, but just in case
    hashCode().toString()
}
