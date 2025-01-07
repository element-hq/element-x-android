/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.safeCapitalize
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface DateFormatterDay {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String
}

@ContributesBinding(AppScope::class)
class DefaultDateFormatterDay @Inject constructor(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) : DateFormatterDay {
    override fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String {
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        val today = localDateTimeProvider.providesNow()
        return if (useRelative) {
            val dayDiff = today.date.toEpochDays() - dateToFormat.date.toEpochDays()
            when (dayDiff) {
                0 -> dateFormatters.getRelativeDay(timestamp, "Today")
                1 -> dateFormatters.getRelativeDay(timestamp, "Yesterday")
                else -> if (dayDiff < 7) {
                    dateFormatters.formatDateWithDay(dateToFormat)
                } else {
                    if (today.year == dateToFormat.year) {
                        dateFormatters.formatDateWithFullFormatNoYear(dateToFormat)
                    } else {
                        dateFormatters.formatDateWithFullFormat(dateToFormat)
                    }
                }
            }
        } else {
            if (today.year == dateToFormat.year) {
                dateFormatters.formatDateWithFullFormatNoYear(dateToFormat)
            } else {
                dateFormatters.formatDateWithFullFormat(dateToFormat)
            }
        }
            .safeCapitalize()
    }
}
