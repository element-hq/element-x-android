/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.tracing

import android.util.Log
import io.element.android.libraries.matrix.api.tracing.Target
import org.matrix.rustcomponents.sdk.LogLevel
import org.matrix.rustcomponents.sdk.logEvent
import timber.log.Timber

/**
 * List of fully qualified class names to ignore when looking for the first stack trace element.
 */
private val fqcnIgnore = listOf(
    Timber::class.java.name,
    Timber.Forest::class.java.name,
    Timber.Tree::class.java.name,
    RustTracingTree::class.java.name,
)

/**
 * A Timber tree that passes logs to the Rust SDK.
 */
internal class RustTracingTree(private val retrieveFromStackTrace: Boolean) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val location = if (retrieveFromStackTrace) {
            getLogEventLocationFromStackTrace()
        } else {
            LogEventLocation("", null)
        }
        val logLevel = priority.toLogLevel()
        logEvent(
            file = location.file,
            line = location.line,
            level = logLevel,
            target = Target.ELEMENT.filter,
            message = if (tag != null) "[$tag] $message" else message,
        )
    }

    /**
     * Extract the [LogEventLocation] from the stack trace.
     */
    private fun getLogEventLocationFromStackTrace(): LogEventLocation {
        return Throwable(null, null).stackTrace
            .first { it.className !in fqcnIgnore }
            .let(LogEventLocation::from)
    }
}

/**
 * Convert a log priority to a Rust SDK log level.
 */
private fun Int.toLogLevel(): LogLevel {
    return when (this) {
        Log.VERBOSE -> LogLevel.TRACE
        Log.DEBUG -> LogLevel.DEBUG
        Log.INFO -> LogLevel.INFO
        Log.WARN -> LogLevel.WARN
        Log.ERROR -> LogLevel.ERROR
        else -> LogLevel.DEBUG
    }
}
