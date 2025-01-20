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
    /**
     * Full date and time.
     * Example:
     * "April 6, 1980 at 6:35 PM"
     * Format can be shorter when useRelative is true.
     * Example:
     * "6:35 PM"
     */
    Full,

    /**
     * Only month and year.
     * Example:
     * "April 1980"
     * "This month" can be returned when useRelative is true.
     * Example:
     * "This month"
     */
    Month,

    /**
     * Only day.
     * Example:
     * "Sunday 6 April"
     * "Today", "Yesterday" and day of week can be returned when useRelative is true.
     */
    Day,

    /**
     * Time if same day, else date.
     */
    TimeOrDate,

    /**
     * Only time whatever the day.
     */
    TimeOnly,
}
