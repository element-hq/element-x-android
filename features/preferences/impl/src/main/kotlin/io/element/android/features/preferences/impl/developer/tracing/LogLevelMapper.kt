/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.matrix.api.tracing.LogLevel

fun LogLevelItem.toLogLevel(): LogLevel {
    return when (this) {
        LogLevelItem.ERROR -> io.element.android.libraries.matrix.api.tracing.LogLevel.ERROR
        LogLevelItem.WARN -> io.element.android.libraries.matrix.api.tracing.LogLevel.WARN
        LogLevelItem.INFO -> io.element.android.libraries.matrix.api.tracing.LogLevel.INFO
        LogLevelItem.DEBUG -> io.element.android.libraries.matrix.api.tracing.LogLevel.DEBUG
        LogLevelItem.TRACE -> io.element.android.libraries.matrix.api.tracing.LogLevel.TRACE
    }
}

fun LogLevel.toLogLevelItem(): LogLevelItem {
    return when (this) {
        LogLevel.ERROR -> LogLevelItem.ERROR
        LogLevel.WARN -> LogLevelItem.WARN
        LogLevel.INFO -> LogLevelItem.INFO
        LogLevel.DEBUG -> LogLevelItem.DEBUG
        LogLevel.TRACE -> LogLevelItem.TRACE
    }
}
