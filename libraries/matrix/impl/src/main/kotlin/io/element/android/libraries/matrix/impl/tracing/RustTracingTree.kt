/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
