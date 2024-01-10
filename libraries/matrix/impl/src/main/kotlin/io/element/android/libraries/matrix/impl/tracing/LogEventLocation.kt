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

/**
 * This class is used to provide file, line, column information to the Rust SDK [org.matrix.rustcomponents.sdk.logEvent] method.
 * The data is extracted from a [StackTraceElement] instance.
 */
data class LogEventLocation(
    val file: String,
    val line: UInt?,
) {

    companion object {
        /**
         * Create a [LogEventLocation] from a [StackTraceElement].
         */
        fun from(stackTraceElement: StackTraceElement): LogEventLocation {
            return LogEventLocation(
                file = stackTraceElement.fileName ?: "",
                line = stackTraceElement.lineNumber.takeIf { it >= 0 }?.toUInt()
            )
        }
    }
}
