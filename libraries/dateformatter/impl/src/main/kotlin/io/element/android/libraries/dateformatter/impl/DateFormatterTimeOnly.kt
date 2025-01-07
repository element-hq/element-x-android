/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import javax.inject.Inject

class DateFormatterTimeOnly @Inject constructor(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) {
    fun format(
        timestamp: Long,
    ): String {
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        return dateFormatters.formatTime(dateToFormat)
    }
}
