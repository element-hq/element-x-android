/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

import java.text.Normalizer
import java.util.Locale

fun Boolean.toOnOff() = if (this) "ON" else "OFF"
fun Boolean.to01() = if (this) "1" else "0"

inline fun <T> T.ooi(block: (T) -> Unit): T = also(block)

/**
 * Return empty CharSequence if the CharSequence is null.
 */
fun CharSequence?.orEmpty() = this ?: ""

/**
 * Useful to append a String at the end of a filename but before the extension if any
 * Ex:
 * - "file.txt".insertBeforeLast("_foo") will return "file_foo.txt"
 * - "file".insertBeforeLast("_foo") will return "file_foo"
 * - "fi.le.txt".insertBeforeLast("_foo") will return "fi.le_foo.txt"
 * - null.insertBeforeLast("_foo") will return "_foo".
 */
fun String?.insertBeforeLast(insert: String, delimiter: String = "."): String {
    if (this == null) return insert
    val idx = lastIndexOf(delimiter)
    return if (idx == -1) {
        this + insert
    } else {
        replaceRange(idx, idx, insert)
    }
}

/**
 * Truncate and ellipsize text if it exceeds the given length.
 *
 * Throws if length is < 1.
 */
fun String.ellipsize(length: Int): String {
    require(length >= 1)

    if (this.length <= length) {
        return this
    }

    return "${this.take(length)}…"
}

/**
 * Replace the old prefix with the new prefix.
 * If the string does not start with the old prefix, the string is returned as is.
 */
fun String.replacePrefix(oldPrefix: String, newPrefix: String): String {
    return if (startsWith(oldPrefix)) {
        newPrefix + substring(oldPrefix.length)
    } else {
        this
    }
}

/**
 * Surround with brackets.
 */
fun String.withBrackets(prefix: String = "(", suffix: String = ")"): String {
    return "$prefix$this$suffix"
}

/**
 * Capitalize the string.
 */
fun String.safeCapitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}

fun String.withoutAccents(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
}

private const val RTL_OVERRIDE_CHAR = '\u202E'
private const val LTR_OVERRIDE_CHAR = '\u202D'

fun String.ensureEndsLeftToRight() = if (containsRtLOverride()) "$this$LTR_OVERRIDE_CHAR" else this

fun String.containsRtLOverride() = contains(RTL_OVERRIDE_CHAR)

fun String.filterDirectionOverrides() = filterNot { it == RTL_OVERRIDE_CHAR || it == LTR_OVERRIDE_CHAR }

/**
 * This works around https://github.com/element-hq/element-x-android/issues/2105.
 * @param maxLength Max characters to retrieve. Defaults to `500`.
 * @param ellipsize Whether to add an ellipsis (`…`) char at the end or not. Defaults to `false`.
 * @return The string truncated to [maxLength] characters, with an optional ellipsis if larger.
 */
fun String.toSafeLength(
    maxLength: Int = 500,
    ellipsize: Boolean = false,
): String {
    return if (ellipsize) {
        ellipsize(maxLength)
    } else if (length > maxLength) {
        take(maxLength)
    } else {
        this
    }
}
