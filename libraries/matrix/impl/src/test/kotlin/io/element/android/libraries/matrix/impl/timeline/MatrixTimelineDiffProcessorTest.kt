/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID_2
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.InsertData
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.SetData
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiffInterface
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.VirtualTimelineItem

open class FakeTimelineDiff(
    private val change: TimelineChange,
    private val item: TimelineItem? = FakeTimelineItem()
) : TimelineDiffInterface {
    override fun change() = change
    override fun append(): List<TimelineItem>? = item?.let { listOf(it) }
    override fun insert(): InsertData? = item?.let { InsertData(1u, it) }
    override fun pushBack(): TimelineItem? = item
    override fun pushFront(): TimelineItem? = item
    override fun remove(): UInt? = 1u
    override fun reset(): List<TimelineItem>? = item?.let { listOf(it) }
    override fun set(): SetData? = item?.let { SetData(1u, it) }
    override fun truncate(): UInt? = 1u
}

class MatrixTimelineDiffProcessorTest {
    private val timelineItems = MutableStateFlow<List<MatrixTimelineItem>>(emptyList())

    private val anEvent = MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
    private val anEvent2 = MatrixTimelineItem.Event(A_UNIQUE_ID_2, anEventTimelineItem())

    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.APPEND)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
        )
    }

    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.PUSH_BACK)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
        )
    }

    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.PUSH_FRONT)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            MatrixTimelineItem.Other,
            anEvent,
        )
    }

    @Test
    fun `Set replaces an entry at some index`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.SET)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other
        )
    }

    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.INSERT)))
        assertThat(timelineItems.value.count()).isEqualTo(3)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
            anEvent2,
        )
    }

    @Test
    fun `Remove removes an entry at some index`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.REMOVE)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            anEvent2,
        )
    }

    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.POP_BACK)))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
        )
    }

    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.POP_FRONT)))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent2,
        )
    }

    @Test
    fun `Clear removes all the entries`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.CLEAR)))
        assertThat(timelineItems.value).isEmpty()
    }

    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.TRUNCATE)))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
        )
    }

    @Test
    fun `Reset removes all entries and add the provided ones`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createProcessor()
        processor.postDiffs(listOf(FakeTimelineDiff(change = TimelineChange.RESET)))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            MatrixTimelineItem.Other,
        )
    }

    private fun TestScope.createProcessor(): MatrixTimelineDiffProcessor {
        val timelineEventContentMapper = TimelineEventContentMapper()
        val timelineItemMapper = MatrixTimelineItemMapper(
            fetchDetailsForEvent = { _ -> Result.success(Unit) },
            coroutineScope = this,
            virtualTimelineItemMapper = VirtualTimelineItemMapper(),
            eventTimelineItemMapper = EventTimelineItemMapper(
                contentMapper = timelineEventContentMapper
            )
        )
        return MatrixTimelineDiffProcessor(
            timelineItems,
            timelineItemFactory = timelineItemMapper,
        )
    }
}

class FakeTimelineItem : TimelineItem(NoPointer) {
    override fun asEvent(): EventTimelineItem? = null
    override fun asVirtual(): VirtualTimelineItem? = null
    override fun fmtDebug(): String = "fmtDebug"
    override fun uniqueId(): String = "uniqueId"
}
