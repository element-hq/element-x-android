package io.element.android.x.matrix.util

import android.util.Log
import io.element.android.x.matrix.LOG_TAG
import org.matrix.rustcomponents.sdk.ClientException

fun logError(throwable: Throwable) {
    when (throwable) {
        is ClientException.Generic -> {
            Log.e(LOG_TAG, "Error ${throwable.msg}", throwable)
        }
        else -> {
            Log.e(LOG_TAG, "Error", throwable)
        }
    }
}