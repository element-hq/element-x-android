/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.hash

import java.security.MessageDigest
import java.util.Locale

/**
 * Compute a Hash of a String, using SHA-512 algorithm.
 */
fun String.hash() = try {
    val digest = MessageDigest.getInstance("SHA-512")
    digest.update(toByteArray())
    digest.digest()
        .joinToString("") { String.format(Locale.ROOT, "%02X", it) }
        .lowercase(Locale.ROOT)
} catch (exc: Exception) {
    // Should not happen, but just in case
    hashCode().toString()
}
