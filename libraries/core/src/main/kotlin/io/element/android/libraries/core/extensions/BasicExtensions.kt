/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

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
