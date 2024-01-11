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

package io.element.android.features.messages.impl.timeline.factories.virtual

import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import javax.inject.Inject

class TimelineItemDaySeparatorFactory @Inject constructor(private val daySeparatorFormatter: DaySeparatorFormatter) {
    fun create(virtualItem: VirtualTimelineItem.DayDivider): TimelineItemVirtualModel {
        val formattedDate = daySeparatorFormatter.format(virtualItem.timestamp)
        return TimelineItemDaySeparatorModel(
            formattedDate = formattedDate
        )
    }
}
