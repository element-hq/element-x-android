/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.browser

import android.util.Log
import android.webkit.ConsoleMessage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import timber.log.Timber

interface ConsoleMessageLogger {
    fun log(
        tag: String,
        consoleMessage: ConsoleMessage,
    )
}

@ContributesBinding(AppScope::class)
class DefaultConsoleMessageLogger : ConsoleMessageLogger {
    override fun log(
        tag: String,
        consoleMessage: ConsoleMessage,
    ) {
        val priority = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
            ConsoleMessage.MessageLevel.WARNING -> Log.WARN
            else -> Log.DEBUG
        }

        val message = buildString {
            append(consoleMessage.sourceId())
            append(":")
            append(consoleMessage.lineNumber())
            append(" ")
            append(consoleMessage.message())
        }

        // Avoid logging any messages that contain "password" to prevent leaking sensitive information
        if (message.contains("password=")) {
            return
        }

        Timber.tag(tag).log(
            priority = priority,
            message = buildString {
                append(consoleMessage.sourceId())
                append(":")
                append(consoleMessage.lineNumber())
                append(" ")
                append(consoleMessage.message())
            },
        )
    }
}
