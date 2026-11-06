/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
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

    @Test
    fun `test structure version`() = runTest {
        val sut = TimelineItemIndexer()
        val event = aTimelineItemEvent()
        val readMarker = TimelineItem.Virtual(
            id = UniqueId("readMarker"),
            model = TimelineItemReadMarkerModel,
        )
        val daySeparator = TimelineItem.Virtual(
            id = UniqueId("daySeparator"),
            model = TimelineItemDaySeparatorModel(formattedDate = "today"),
        )

        // An initial empty list is not a structural change.
        assertThat(sut.process(emptyList())).isEqualTo(0)

        // Adding items is.
        assertThat(sut.process(listOf(event, readMarker, daySeparator))).isEqualTo(1)

        // Processing the very same list again is not.
        assertThat(sut.process(listOf(event, readMarker, daySeparator))).isEqualTo(1)

        // Neither is updating an item's content in place: the identifier is unchanged.
        val editedEvent = event.copy(sentTime = "12:35")
        assertThat(sut.process(listOf(editedEvent, readMarker, daySeparator))).isEqualTo(1)

        // Moving the read marker between two virtual items is a structural change, even though no
        // event index changes and the event id -> index map is therefore identical.
        assertThat(sut.indexOf(event.eventId!!)).isEqualTo(0)
        assertThat(sut.process(listOf(editedEvent, daySeparator, readMarker))).isEqualTo(2)
        assertThat(sut.indexOf(event.eventId!!)).isEqualTo(0)

        // Removing an item is a structural change too.
        assertThat(sut.process(listOf(editedEvent, daySeparator))).isEqualTo(3)
    }
}
