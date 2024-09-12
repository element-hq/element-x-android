/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultDaySeparatorFormatter @Inject constructor(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) : DaySeparatorFormatter {
    override fun format(timestamp: Long): String {
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        // TODO use relative formatting once iOS uses it too
        return dateFormatters.formatDateWithFullFormat(dateToFormat)
    }
}
