/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import io.element.android.libraries.core.extensions.safeCapitalize
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

class DateFormatterMonth @Inject constructor(
    private val stringProvider: StringProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String {
        val today = localDateTimeProvider.providesNow()
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        return if (useRelative && dateToFormat.month == today.month && dateToFormat.year == today.year) {
            stringProvider.getString(R.string.common_date_this_month)
        } else {
            dateFormatters.formatDateWithMonthAndYear(dateToFormat)
        }
            .safeCapitalize()
    }
}
