/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.log.logger

/**
 * Parent class for custom logger tags. Can be used with Timber :
 *
 * val loggerTag = LoggerTag("MyTag", LoggerTag.VOIP)
 * Timber.tag(loggerTag.value).v("My log message")
 */
open class LoggerTag(name: String, parentTag: LoggerTag? = null) {
    object PushLoggerTag : LoggerTag("Push")
    object NotificationLoggerTag : LoggerTag("Notification", PushLoggerTag)

    val value: String = if (parentTag == null) {
        name
    } else {
        "${parentTag.value}/$name"
    }
}
