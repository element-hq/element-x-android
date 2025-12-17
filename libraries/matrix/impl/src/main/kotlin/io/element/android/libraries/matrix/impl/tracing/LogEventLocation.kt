/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
