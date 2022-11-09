package io.element.android.x.core.data

/**
 * Wrapper for a CharSequence, which support mutation of the CharSequence.
 */
class StableCharSequence(val charSequence: CharSequence) {
    private val hash = charSequence.toString().hashCode()

    override fun hashCode() = hash
    override fun equals(other: Any?) = other is StableCharSequence && other.hash == hash
}

fun CharSequence.toStableCharSequence() = StableCharSequence(this)
