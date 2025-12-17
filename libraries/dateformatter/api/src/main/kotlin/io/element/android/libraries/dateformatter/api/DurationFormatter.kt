/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.api

import java.util.Locale
import kotlin.time.Duration

/**
 * Convert milliseconds to human readable duration.
 * Hours in 1 digit or more.
 * Minutes in 2 digits when hours are available.
 * Seconds always on 2 digits.
 * Example:
 * - when the duration is longer than 1 hour:
 * - "10:23:34"
 * - "1:23:34"
 * - "1:03:04"
 * - when the duration is shorter:
 * - "4:56"
 * - "14:06"
 * - Less than one minute:
 * - "0:00"
 * - "0:01"
 * - "0:59"
 */
fun Long.toHumanReadableDuration(): String {
    val inSeconds = this / 1_000
    val hours = inSeconds / 3_600
    val minutes = inSeconds % 3_600 / 60
    val seconds = inSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

fun Duration.toHumanReadableDuration() = inWholeMilliseconds.toHumanReadableDuration()
