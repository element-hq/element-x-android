/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultDateFormatter @Inject constructor(
    private val dateFormatterFull: DateFormatterFull,
    private val dateFormatterMonth: DateFormatterMonth,
    private val dateFormatterDay: DateFormatterDay,
    private val dateFormatterTime: DateFormatterTime,
    private val dateFormatterTimeOnly: DateFormatterTimeOnly,
) : DateFormatter {
    override fun format(
        timestamp: Long?,
        mode: DateFormatterMode,
        useRelative: Boolean,
    ): String {
        timestamp ?: return ""
        return when (mode) {
            DateFormatterMode.Full -> {
                dateFormatterFull.format(timestamp, useRelative)
            }
            DateFormatterMode.Month -> {
                dateFormatterMonth.format(timestamp, useRelative)
            }
            DateFormatterMode.Day -> {
                dateFormatterDay.format(timestamp, useRelative)
            }
            DateFormatterMode.TimeOrDate -> {
                dateFormatterTime.format(timestamp, useRelative)
            }
            DateFormatterMode.TimeOnly -> {
                dateFormatterTimeOnly.format(timestamp)
            }
        }
    }
}
