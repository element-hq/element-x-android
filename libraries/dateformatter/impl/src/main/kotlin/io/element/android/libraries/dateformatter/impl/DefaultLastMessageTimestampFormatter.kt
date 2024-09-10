/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLastMessageTimestampFormatter @Inject constructor(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) : LastMessageTimestampFormatter {
    override fun format(timestamp: Long?): String {
        if (timestamp == null) return ""
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
                    useRelative = true
                )
            }
        }
    }
}
