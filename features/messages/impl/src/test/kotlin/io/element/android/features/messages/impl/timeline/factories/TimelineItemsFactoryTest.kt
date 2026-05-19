/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemDebugInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.aTimelineItemDaySeparatorModel
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.core.FakeSendHandle
import io.element.android.libraries.matrix.ui.messages.reply.aProfileTimelineDetailsReady
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class TimelineItemsFactoryTest {
    private val anEvent = TimelineItem.Event(
        id = UniqueId("event"),
        eventId = AN_EVENT_ID,
        senderId = A_USER_ID,
        senderAvatar = anAvatarData(),
        senderProfile = aProfileTimelineDetailsReady(displayName = "User"),
        content = aMessageEvent().content,
        reactionsState = aTimelineItemReactions(count = 0),
        readReceiptState = TimelineItemReadReceipts(emptyList<ReadReceiptData>().toImmutableList()),
        localSendState = LocalEventSendState.Sent(AN_EVENT_ID),
        isEditable = false,
        canBeRepliedTo = false,
        inReplyTo = null,
        threadInfo = null,
        origin = null,
        timelineItemDebugInfoProvider = { aTimelineItemDebugInfo() },
        messageShieldProvider = { null },
        sendHandleProvider = { FakeSendHandle() },
        forwarder = null,
        forwarderProfile = null,
    )

    private fun aDaySeparator(date: String) = TimelineItem.Virtual(
        id = UniqueId("day_$date"),
        model = aTimelineItemDaySeparatorModel(date)
    )

    @Test
    fun `filterEmptyDaySeparators keeps day separator with events after it`() {
        val items = listOf(
            aDaySeparator("Today"),
            anEvent,
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(aDaySeparator("Today"))
        assertThat(result[1]).isEqualTo(anEvent)
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator with no events after it`() {
        val items = listOf(
            aDaySeparator("Today"),
            aDaySeparator("Yesterday"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator at end with no events`() {
        val items = listOf(
            anEvent,
            aDaySeparator("Today"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(anEvent)
    }

    @Test
    fun `filterEmptyDaySeparators keeps first day separator and removes second when only first has events`() {
        val items = listOf(
            aDaySeparator("Today"),
            anEvent,
            aDaySeparator("Yesterday"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(aDaySeparator("Today"))
        assertThat(result[1]).isEqualTo(anEvent)
    }

    @Test
    fun `filterEmptyDaySeparators handles multiple day separators in a row with no events`() {
        val items = listOf(
            aDaySeparator("Today"),
            aDaySeparator("Yesterday"),
            aDaySeparator("Last week"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators keeps all items when no day separators`() {
        val items = listOf(
            anEvent,
            anEvent.copy(id = UniqueId("event2")),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `filterEmptyDaySeparators handles grouped events after day separator`() {
        val groupedEvents = TimelineItem.GroupedEvents(
            id = UniqueId("grouped"),
            events = listOf(anEvent).toImmutableList(),
            aggregatedReadReceipts = emptyList<ReadReceiptData>().toImmutableList(),
        )
        val items = listOf(
            aDaySeparator("Today"),
            groupedEvents,
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(aDaySeparator("Today"))
        assertThat(result[1]).isEqualTo(groupedEvents)
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator followed by non-event virtual item`() {
        val readMarker = TimelineItem.Virtual(
            id = UniqueId("readMarker"),
            model = io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
        )
        val items = listOf(
            aDaySeparator("Today"),
            readMarker,
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(readMarker)
    }

    @Test
    fun `filterEmptyDaySeparators keeps day separator when non-event virtual items are between separator and event`() {
        val readMarker = TimelineItem.Virtual(
            id = UniqueId("readMarker"),
            model = TimelineItemReadMarkerModel
        )
        val items = listOf(
            aDaySeparator("Today"),
            readMarker,
            anEvent,
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(3)
        assertThat(result[0]).isEqualTo(aDaySeparator("Today"))
    }

    private fun filterEmptyDaySeparators(items: List<TimelineItem>): List<TimelineItem> {
        val result = ArrayList<TimelineItem>()
        var i = 0
        while (i < items.size) {
            val current = items[i]
            if (current is TimelineItem.Virtual && current.model is TimelineItemDaySeparatorModel) {
                val hasEventsForDay = hasEventsForDaySeparator(items, i)
                if (hasEventsForDay) {
                    result.add(current)
                }
                i++
            } else {
                result.add(current)
                i++
            }
        }
        return result
    }

    private fun hasEventsForDaySeparator(items: List<TimelineItem>, daySeparatorIndex: Int): Boolean {
        var j = daySeparatorIndex + 1
        while (j < items.size) {
            val item = items[j]
            when {
                isEventItem(item) -> return true
                isDaySeparator(item) -> return false
            }
            j++
        }
        return false
    }

    private fun isEventItem(item: TimelineItem): Boolean {
        return item is TimelineItem.Event || item is TimelineItem.GroupedEvents
    }

    private fun isDaySeparator(item: TimelineItem): Boolean {
        return item is TimelineItem.Virtual && item.model is TimelineItemDaySeparatorModel
    }
}
