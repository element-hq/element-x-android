package io.element.android.x.sdk.matrix.util

import android.util.Log
import io.element.android.x.sdk.matrix.LOG_TAG
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