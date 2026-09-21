/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class TimelineLazyRowTest {
    @Test
    fun `flatten puts receipt rows before their events for reverseLayout`() {
        val typing = TimelineItem.Virtual(UniqueId("TypingNotification"), TimelineItemTypingNotificationModel)
        val older = aTimelineItemEvent(content = aTimelineItemTextContent(body = "older"))
        val newest = aTimelineItemEvent(
            content = aTimelineItemTextContent(body = "newest"),
            readReceiptState = aTimelineItemReadReceipts(listOf(aReadReceiptData(0))),
        )
        val rows = flattenTimelineItemsForLazyColumn(
            timelineItems = persistentListOf(typing, newest, older),
            lastOutgoingEventId = null,
        )

        assertThat(rows.rows.map { it.key }).containsExactly(
            typing.identifier(),
            "rr_${newest.identifier()}",
            newest.identifier(),
            older.identifier(),
        ).inOrder()
    }

    @Test
    fun `last outgoing send state creates a receipt row`() {
        val mine = aTimelineItemEvent(
            content = aTimelineItemTextContent(body = "mine"),
            isMine = true,
            sendState = LocalEventSendState.Sent(EventId("\$1")),
        )
        val rows = flattenTimelineItemsForLazyColumn(
            timelineItems = persistentListOf(mine),
            lastOutgoingEventId = mine.identifier(),
        )

        assertThat(rows.size).isEqualTo(2)
        assertThat(rows[0]).isInstanceOf(TimelineLazyRow.ReadReceipt::class.java)
        assertThat(rows[1]).isInstanceOf(TimelineLazyRow.Item::class.java)
    }

    @Test
    fun `index mapping round-trips timeline and lazy indices`() {
        val typing = TimelineItem.Virtual(UniqueId("TypingNotification"), TimelineItemTypingNotificationModel)
        val event = aTimelineItemEvent(
            content = aTimelineItemTextContent(body = "event"),
            readReceiptState = aTimelineItemReadReceipts(listOf(aReadReceiptData(0))),
        )
        val rows = flattenTimelineItemsForLazyColumn(
            timelineItems = persistentListOf(typing, event),
            lastOutgoingEventId = null,
        )

        assertThat(rows.timelineIndexAt(0)).isEqualTo(0)
        assertThat(rows.timelineIndexAt(1)).isEqualTo(1) // receipt shares event timeline index
        assertThat(rows.timelineIndexAt(2)).isEqualTo(1)
        assertThat(rows.lazyIndexForTimelineIndex(1)).isEqualTo(2)
    }

    @Test
    fun `holder reuses item rows when timeline item references are unchanged`() {
        val older = aTimelineItemEvent(content = aTimelineItemTextContent(body = "older"))
        val newest = aTimelineItemEvent(
            content = aTimelineItemTextContent(body = "newest"),
            readReceiptState = aTimelineItemReadReceipts(listOf(aReadReceiptData(0))),
        )
        val items = persistentListOf(newest, older)
        val holder = TimelineLazyRowsHolder()
        val first = holder.getOrUpdate(items, lastOutgoingEventId = null)
        val second = holder.getOrUpdate(items, lastOutgoingEventId = null)

        assertThat(second).isSameInstanceAs(first)
    }

    @Test
    fun `holder reuses unchanged item rows across a newer message insert`() {
        val older = aTimelineItemEvent(content = aTimelineItemTextContent(body = "older"))
        val mid = aTimelineItemEvent(content = aTimelineItemTextContent(body = "mid"))
        val holder = TimelineLazyRowsHolder()
        val first = holder.getOrUpdate(persistentListOf(mid, older), lastOutgoingEventId = null)
        val newer = aTimelineItemEvent(
            content = aTimelineItemTextContent(body = "newer"),
            readReceiptState = aTimelineItemReadReceipts(listOf(aReadReceiptData(0))),
        )
        val second = holder.getOrUpdate(persistentListOf(newer, mid, older), lastOutgoingEventId = null)

        val firstOlder = first.rows.first { it is TimelineLazyRow.Item && it.timelineItem === older }
        val secondOlder = second.rows.first { it is TimelineLazyRow.Item && it.timelineItem === older }
        assertThat(secondOlder).isSameInstanceAs(firstOlder)

        val firstMid = first.rows.first { it is TimelineLazyRow.Item && it.timelineItem === mid }
        val secondMid = second.rows.first { it is TimelineLazyRow.Item && it.timelineItem === mid }
        assertThat(secondMid).isSameInstanceAs(firstMid)
    }
}
