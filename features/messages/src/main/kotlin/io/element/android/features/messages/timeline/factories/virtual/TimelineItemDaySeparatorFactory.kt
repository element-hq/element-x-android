/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.timeline.factories.virtual

import io.element.android.features.messages.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.timeline.model.virtual.TimelineItemVirtualModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.matrix.rustcomponents.sdk.VirtualTimelineItem
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class TimelineItemDaySeparatorFactory @Inject constructor() {

    //TODO use proper formatter
    private val locale: Locale = Locale.getDefault()

    private val dateWithYearFormatter: DateTimeFormatter by lazy {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "dd.MM.yyyy")
        DateTimeFormatter.ofPattern(pattern)
    }

    fun create(virtualItem: VirtualTimelineItem.DayDivider): TimelineItemVirtualModel {
        val tsInstant = Instant.fromEpochMilliseconds(virtualItem.ts.toLong())
        val tsDateTime = tsInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        return TimelineItemDaySeparatorModel(
            formattedDate = dateWithYearFormatter.format(tsDateTime.toJavaLocalDateTime())
        )
    }
}
