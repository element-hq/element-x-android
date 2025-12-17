/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import dev.zacsweers.metro.Inject

@Inject
class DateFormatterTimeOnly(
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
