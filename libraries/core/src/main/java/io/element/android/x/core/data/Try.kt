package io.element.android.x.core.data

import timber.log.Timber

inline fun <A> tryOrNull(message: String? = null, operation: () -> A): A? {
    return try {
        operation()
    } catch (any: Throwable) {
        if (message != null) {
            Timber.e("TAG", message, any)
        }
        null
    }
}
