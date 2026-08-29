/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.api

/**
 * Formats timestamps for display, in the user's locale and time zone.
 */
interface DateFormatter {
    /**
     * Formats a timestamp according to [mode].
     *
     * @param timestamp milliseconds since the epoch; a `null` value yields an empty string, so callers need no null check of their own.
     * @param mode how much of the date and time to include; see [DateFormatterMode] for examples of each.
     * @param useRelative true to prefer wording relative to today, such as "Today" or "This month", where the mode allows it.
     */
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
