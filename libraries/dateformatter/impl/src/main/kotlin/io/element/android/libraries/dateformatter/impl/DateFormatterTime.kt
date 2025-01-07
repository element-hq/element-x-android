/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import javax.inject.Inject

class DateFormatterTime @Inject constructor(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String {
        val currentDate = localDateTimeProvider.providesNow()
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        val isSameDay = currentDate.date == dateToFormat.date
        return when {
            isSameDay -> {
                dateFormatters.formatTime(dateToFormat)
            }
            else -> {
                dateFormatters.formatDate(
                    dateToFormat = dateToFormat,
                    currentDate = currentDate,
                    useRelative = useRelative,
                )
            }
        }
    }
}
