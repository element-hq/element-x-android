/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelineItemIndexerTest {
    @Test
    fun `test TimelineItemIndexer`() = runTest {
        val eventIds = mutableListOf<EventId>()
        val data = listOf(
            aTimelineItemEvent().also { eventIds.add(it.eventId!!) },
            aTimelineItemEvent().also { eventIds.add(it.eventId!!) },
            aGroupedEvents().also { groupedEvents ->
                groupedEvents.events.forEach { eventIds.add(it.eventId!!) }
            },
            TimelineItem.Virtual(
                id = UniqueId("dummy"),
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
