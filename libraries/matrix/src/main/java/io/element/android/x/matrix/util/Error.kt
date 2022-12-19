package io.element.android.x.matrix.util

import org.matrix.rustcomponents.sdk.ClientException
import timber.log.Timber

fun logError(throwable: Throwable) {
    when (throwable) {
        is ClientException.Generic -> {
            Timber.e("Error ${throwable.msg}", throwable)
        }
        else -> {
            Timber.e("Error", throwable)
        }
    }
}
