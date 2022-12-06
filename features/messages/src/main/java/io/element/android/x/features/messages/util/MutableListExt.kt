package io.element.android.x.features.messages.util

internal inline fun <reified T> MutableList<T?>.invalidateLast() {
    val indexOfLast = size
    if (indexOfLast > 0) {
        set(indexOfLast - 1, null)
    }
}