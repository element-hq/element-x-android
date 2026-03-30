/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.format.DateUtils
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.libraries.dateformatter.api.DurationFormatter
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Formats durations in a localized, human-readable way using Android's MeasureFormat.
 *
 * Uses WIDE format for readability (e.g., "5 hours", "3 minutes", "10 seconds").
 * Rounds to the nearest unit for cleaner display.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<DurationFormatter>())
class DefaultDurationFormatter(
    localeChangeObserver: LocaleChangeObserver,
    locale: Locale,
) : DurationFormatter, LocaleChangeListener {
    init {
        localeChangeObserver.addListener(this)
    }

    // Cache formatter, recreate only on locale change
    private var formatter: MeasureFormat = MeasureFormat.getInstance(locale, FormatWidth.WIDE)

    override fun onLocaleChange() {
        formatter = MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.WIDE)
    }

    override fun format(duration: Duration): String {
        val millis = duration.inWholeMilliseconds

        return when {
            duration >= 1.hours -> {
                // Round to nearest hour (add 30 minutes before dividing)
                val hours = ((millis + 30 * DateUtils.MINUTE_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS).toInt()
                formatter.format(Measure(hours, MeasureUnit.HOUR))
            }
            duration >= 1.minutes -> {
                // Round to nearest minute (add 30 seconds before dividing)
                val minutes = ((millis + 30 * DateUtils.SECOND_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS).toInt()
                formatter.format(Measure(minutes, MeasureUnit.MINUTE))
            }
            else -> {
                // Round to nearest second (add 500ms before dividing)
                val seconds = ((millis + 500) / DateUtils.SECOND_IN_MILLIS).toInt()
                formatter.format(Measure(seconds, MeasureUnit.SECOND))
            }
        }
    }
}
