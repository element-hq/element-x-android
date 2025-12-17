/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.safeCapitalize

interface DateFormatterDay {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String
}

@ContributesBinding(AppScope::class)
class DefaultDateFormatterDay(
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
                0L -> dateFormatters.getRelativeDay(timestamp, "Today")
                1L -> dateFormatters.getRelativeDay(timestamp, "Yesterday")
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
