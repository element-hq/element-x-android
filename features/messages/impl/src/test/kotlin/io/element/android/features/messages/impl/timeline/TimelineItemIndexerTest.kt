/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import org.junit.Test

class TimelineItemIndexerTest {
    @Test
    fun `test TimelineItemIndexer`() {
        val eventIds = mutableListOf<EventId>()
        val data = listOf(
            aTimelineItemEvent().also { eventIds.add(it.eventId!!) },
            aTimelineItemEvent().also { eventIds.add(it.eventId!!) },
            aGroupedEvents().also { groupedEvents ->
                groupedEvents.events.forEach { eventIds.add(it.eventId!!) }
            },
            TimelineItem.Virtual(
                id = "dummy",
                model = TimelineItemReadMarkerModel
            ),
        )
        assertThat(eventIds.size).isEqualTo(4)
        val sut = TimelineItemIndexer()
        sut.process(data)
        eventIds.forEach {
            assertThat(sut.isKnown(it)).isTrue()
        }
        assertThat(sut.indexOf(eventIds[0])).isEqualTo(0)
        assertThat(sut.indexOf(eventIds[1])).isEqualTo(1)
        assertThat(sut.indexOf(eventIds[2])).isEqualTo(2)
        assertThat(sut.indexOf(eventIds[3])).isEqualTo(2)

        // Unknown event
        assertThat(sut.isKnown(AN_EVENT_ID)).isFalse()
        assertThat(sut.indexOf(AN_EVENT_ID)).isEqualTo(-1)
    }
}
