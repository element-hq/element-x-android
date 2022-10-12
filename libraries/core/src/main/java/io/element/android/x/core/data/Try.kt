package io.element.android.x.core.data

import android.util.Log

inline fun <A> tryOrNull(message: String? = null, operation: () -> A): A? {
    return try {
        operation()
    } catch (any: Throwable) {
        if (message != null) {
            Log.e("TAG", message, any)
        }
        null
    }
}
