/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.groups.collapseRedactedRuns
import io.element.android.features.messages.impl.timeline.groups.computeGroupIdWith
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class CollapseRedactedRunsTest {
    private fun redacted(id: String) = aTimelineItemEvent(
        eventId = EventId(id),
        content = TimelineItemRedactedContent,
    )

    @Test
    fun `collapses a run of three or more redacted events into a single group`() {
        val r1 = redacted("\$R1")
        val r2 = redacted("\$R2")
        val r3 = redacted("\$R3")
        val result = listOf<TimelineItem>(r1, r2, r3).collapseRedactedRuns()
        assertThat(result).hasSize(1)
        val group = result.single() as TimelineItem.GroupedEvents
        // Stored oldest-first (the input is newest-first), like the existing grouper.
        assertThat(group.events.map { it.eventId }).containsExactly(
            EventId("\$R3"),
            EventId("\$R2"),
            EventId("\$R1"),
        ).inOrder()
        // Group id is derived from the newest event of the run so it survives older pagination.
        assertThat(group.id).isEqualTo(computeGroupIdWith(r1))
    }

    @Test
    fun `leaves a run shorter than three as individual tiles`() {
        val r1 = redacted("\$R1")
        val r2 = redacted("\$R2")
        val result = listOf<TimelineItem>(r1, r2).collapseRedactedRuns()
        assertThat(result).containsExactly(r1, r2).inOrder()
    }

    @Test
    fun `keeps surrounding events and only collapses the redacted run between them`() {
        val newest = aTimelineItemEvent(eventId = EventId("\$NEW"))
        val r1 = redacted("\$R1")
        val r2 = redacted("\$R2")
        val r3 = redacted("\$R3")
        val oldest = aTimelineItemEvent(eventId = EventId("\$OLD"))
        val result = listOf<TimelineItem>(newest, r1, r2, r3, oldest).collapseRedactedRuns()
        assertThat(result).hasSize(3)
        assertThat(result.first()).isEqualTo(newest)
        assertThat(result.last()).isEqualTo(oldest)
        assertThat(result[1]).isInstanceOf(TimelineItem.GroupedEvents::class.java)
        assertThat((result[1] as TimelineItem.GroupedEvents).events).hasSize(3)
    }

    @Test
    fun `a day separator breaks a run so each side is evaluated on its own`() {
        val r1 = redacted("\$R1")
        val r2 = redacted("\$R2")
        val r3 = redacted("\$R3")
        val sep = aTimelineItemDaySeparator()
        val r4 = redacted("\$R4")
        val r5 = redacted("\$R5")
        val result = listOf<TimelineItem>(r1, r2, r3, sep, r4, r5).collapseRedactedRuns()
        // First run (3) collapses, the separator passes through, the second run (2) stays as tiles.
        assertThat(result).hasSize(4)
        assertThat(result.first()).isInstanceOf(TimelineItem.GroupedEvents::class.java)
        assertThat(result[1]).isEqualTo(sep)
        assertThat(result[2]).isEqualTo(r4)
        assertThat(result[3]).isEqualTo(r5)
    }

    @Test
    fun `does not collapse a run interrupted by a non-redacted event`() {
        val r1 = redacted("\$R1")
        val r2 = redacted("\$R2")
        val between = aTimelineItemEvent(eventId = EventId("\$MID"))
        val r3 = redacted("\$R3")
        val result = listOf<TimelineItem>(r1, r2, between, r3).collapseRedactedRuns()
        assertThat(result).containsExactly(r1, r2, between, r3).inOrder()
    }

    @Test
    fun `collapses a long run into a single group keeping every event`() {
        val run = (1..5).map { redacted("\$R$it") }
        val result = run.collapseRedactedRuns()
        assertThat(result).hasSize(1)
        assertThat((result.single() as TimelineItem.GroupedEvents).events).hasSize(5)
    }

    @Test
    fun `collapses each qualifying run independently`() {
        val firstRun = (1..3).map { redacted("\$A$it") }
        val separator = aTimelineItemEvent(eventId = EventId("\$SEP"))
        val secondRun = (1..3).map { redacted("\$B$it") }
        val result = (firstRun + separator + secondRun).collapseRedactedRuns()
        assertThat(result).hasSize(3)
        assertThat(result[0]).isInstanceOf(TimelineItem.GroupedEvents::class.java)
        assertThat(result[1]).isEqualTo(separator)
        assertThat(result[2]).isInstanceOf(TimelineItem.GroupedEvents::class.java)
        // The two groups get distinct ids so their expand state does not bleed into one another.
        assertThat((result[0] as TimelineItem.GroupedEvents).id)
            .isNotEqualTo((result[2] as TimelineItem.GroupedEvents).id)
    }

    @Test
    fun `collapses a run that sits at the very end of the list`() {
        val newest = aTimelineItemEvent(eventId = EventId("\$NEW"))
        val run = (1..3).map { redacted("\$R$it") }
        val result = (listOf<TimelineItem>(newest) + run).collapseRedactedRuns()
        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualTo(newest)
        assertThat(result.last()).isInstanceOf(TimelineItem.GroupedEvents::class.java)
    }

    @Test
    fun `leaves a single redacted event untouched`() {
        val r1 = redacted("\$R1")
        assertThat(listOf<TimelineItem>(r1).collapseRedactedRuns()).containsExactly(r1)
    }

    @Test
    fun `aggregates the read receipts of every event in the collapsed group`() {
        fun redactedWithReceipt(id: String, receiptIndex: Int) = aTimelineItemEvent(
            eventId = EventId(id),
            content = TimelineItemRedactedContent,
            readReceiptState = TimelineItemReadReceipts(
                receipts = listOf(aReadReceiptData(receiptIndex)).toImmutableList(),
            ),
        )
        val run = listOf<TimelineItem>(
            redactedWithReceipt("\$R1", 0),
            redactedWithReceipt("\$R2", 1),
            redactedWithReceipt("\$R3", 2),
        )
        val group = run.collapseRedactedRuns().single() as TimelineItem.GroupedEvents
        assertThat(group.aggregatedReadReceipts).hasSize(3)
    }

    @Test
    fun `an existing grouped item breaks the run and is passed through untouched`() {
        val stateGroup = aGroupedEvents(id = UniqueId("state"))
        val run = (1..3).map { redacted("\$R$it") }
        val result = (run + stateGroup).collapseRedactedRuns()
        assertThat(result).hasSize(2)
        assertThat(result.first()).isInstanceOf(TimelineItem.GroupedEvents::class.java)
        assertThat(result.last()).isEqualTo(stateGroup)
    }

    @Test
    fun `is a no-op on an empty list`() {
        assertThat(emptyList<TimelineItem>().collapseRedactedRuns()).isEmpty()
    }
}
