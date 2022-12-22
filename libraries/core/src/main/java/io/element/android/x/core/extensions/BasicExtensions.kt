package io.element.android.x.core.extensions

import android.util.Patterns

fun Boolean.toOnOff() = if (this) "ON" else "OFF"

inline fun <T> T.ooi(block: (T) -> Unit): T = also(block)

/**
 * Check if a CharSequence is an email.
 */
fun CharSequence.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

// fun CharSequence.isMatrixId() = MatrixPatterns.isUserId(this.toString())

/**
 * Return empty CharSequence if the CharSequence is null.
 */
fun CharSequence?.orEmpty() = this ?: ""

/**
 * Check if a CharSequence is a phone number.
 */
/*
fun CharSequence.isMsisdn(): Boolean {
    return try {
        PhoneNumberUtil.getInstance().parse(ensurePrefix("+"), null)
        true
    } catch (e: NumberParseException) {
        false
    }
}
 */

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

inline fun <reified R> Any?.takeAs(): R? {
    return takeIf { it is R } as R?
}
