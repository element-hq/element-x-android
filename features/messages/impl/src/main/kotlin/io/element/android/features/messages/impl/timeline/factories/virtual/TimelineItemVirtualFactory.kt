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

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemUnknownVirtualModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import org.matrix.rustcomponents.sdk.VirtualTimelineItem
import javax.inject.Inject

class TimelineItemVirtualFactory @Inject constructor(
    private val daySeparatorFactory: TimelineItemDaySeparatorFactory,
) {

    fun create(
        currentTimelineItem: MatrixTimelineItem.Virtual,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
    ): TimelineItem.Virtual {
        return TimelineItem.Virtual(
            id = "virtual_item_$index",
            model = currentTimelineItem.computeModel(index)
        )
    }

    private fun MatrixTimelineItem.Virtual.computeModel(index: Int): TimelineItemVirtualModel {
        return when (val inner = virtual) {
            is VirtualTimelineItem.DayDivider -> daySeparatorFactory.create(inner)
            is VirtualTimelineItem.ReadMarker -> TimelineItemReadMarkerModel
            is VirtualTimelineItem.LoadingIndicator -> TimelineItemLoadingModel
            is VirtualTimelineItem.TimelineStart -> TimelineItemReadMarkerModel
            else -> TimelineItemUnknownVirtualModel
        }
    }
}
