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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.TimelineItem

class MatrixTimelineItemMapper(
    private val fetchDetailsForEvent: suspend (EventId) -> Result<Unit>,
    private val coroutineScope: CoroutineScope,
    private val virtualTimelineItemMapper: VirtualTimelineItemMapper = VirtualTimelineItemMapper(),
    private val eventTimelineItemMapper: EventTimelineItemMapper = EventTimelineItemMapper(),
) {
    fun map(timelineItem: TimelineItem): MatrixTimelineItem = timelineItem.use {
        val uniqueId = timelineItem.uniqueId()
        val asEvent = it.asEvent()
        if (asEvent != null) {
            val eventTimelineItem = eventTimelineItemMapper.map(asEvent)
            if (eventTimelineItem.hasNotLoadedInReplyTo() && eventTimelineItem.eventId != null) {
                fetchEventDetails(eventTimelineItem.eventId!!)
            }

            return MatrixTimelineItem.Event(uniqueId, eventTimelineItem)
        }
        val asVirtual = it.asVirtual()
        if (asVirtual != null) {
            val virtualTimelineItem = virtualTimelineItemMapper.map(asVirtual)
            return MatrixTimelineItem.Virtual(uniqueId, virtualTimelineItem)
        }
        return MatrixTimelineItem.Other
    }

    private fun fetchEventDetails(eventId: EventId) = coroutineScope.launch {
        fetchDetailsForEvent(eventId)
    }
}
