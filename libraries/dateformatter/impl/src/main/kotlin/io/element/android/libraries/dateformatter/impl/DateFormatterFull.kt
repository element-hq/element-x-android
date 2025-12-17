/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import dev.zacsweers.metro.Inject
import io.element.android.services.toolbox.api.strings.StringProvider

@Inject
class DateFormatterFull(
    private val stringProvider: StringProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
    private val dateFormatterDay: DateFormatterDay,
) {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String {
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        val time = dateFormatters.formatTime(dateToFormat)
        return if (useRelative) {
            val now = localDateTimeProvider.providesNow()
            if (now.date == dateToFormat.date) {
               time
            } else {
                val dateStr = dateFormatterDay.format(timestamp, true)
                stringProvider.getString(R.string.common_date_date_at_time, dateStr, time)
            }
        } else {
            val dateStr = dateFormatters.formatDateWithFullFormat(dateToFormat)
            stringProvider.getString(R.string.common_date_date_at_time, dateStr, time)
        }
    }
}
