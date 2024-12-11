/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.api

interface DateFormatter {
    fun format(
        timestamp: Long?,
        mode: DateFormatterMode = DateFormatterMode.Full,
        useRelative: Boolean = false,
    ): String
}

enum class DateFormatterMode {
    Full,
    Month,
    Day,
    // Time if same day, else date
    TimeOrDate,
    // Only time whatever the day
    TimeOnly,
}
