/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.text

import android.text.Spannable
import androidx.core.text.toSpannable
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

fun String.urlEncoded(charset: Charset = Charsets.UTF_8): String = URLEncoder.encode(this, charset.name())
fun String.urlDecoded(charset: Charset = Charsets.UTF_8): String = URLDecoder.decode(this, charset.name())

fun String.takeIfNotBlank(): String? = ifBlank { null }

/**
 * Convert the CharSequence to a Spannable, or return null if it fails (e.g. in unit tests).
 * This was the previous behaviour of toSpannable() before core-ktx:1.19.0, which now returns a SpannableString with `"null"` value.
 */
fun CharSequence.safeToSpannable(): Spannable? = try {
    toSpannable()
} catch (_: Exception) {
    // This happens in unit tests
    null
}
