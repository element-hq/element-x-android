/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineItem
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID_2
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.TimelineDiff

class MatrixTimelineDiffProcessorTest {
    private val timelineItems = MutableStateFlow<List<MatrixTimelineItem>>(emptyList())

    private val anEvent = MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
    private val anEvent2 = MatrixTimelineItem.Event(A_UNIQUE_ID_2, anEventTimelineItem())

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Append(listOf(FakeFfiTimelineItem()))))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.PushBack(FakeFfiTimelineItem())))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        timelineItems.value = listOf(anEvent)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.PushFront(FakeFfiTimelineItem())))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            MatrixTimelineItem.Other,
            anEvent,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Set replaces an entry at some index`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Set(1u, FakeFfiTimelineItem())))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Insert(1u, FakeFfiTimelineItem())))
        assertThat(timelineItems.value.count()).isEqualTo(3)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            MatrixTimelineItem.Other,
            anEvent2,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Remove removes an entry at some index`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Remove(1u)))
        assertThat(timelineItems.value.count()).isEqualTo(2)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
            anEvent2,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.PopBack))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.PopFront))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent2,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Clear removes all the entries`() = runTest {
        timelineItems.value = listOf(anEvent, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Clear))
        assertThat(timelineItems.value).isEmpty()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Truncate(1u)))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            anEvent,
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Reset removes all entries and add the provided ones`() = runTest {
        timelineItems.value = listOf(anEvent, MatrixTimelineItem.Other, anEvent2)
        val processor = createMatrixTimelineDiffProcessor(timelineItems)
        processor.postDiffs(listOf(TimelineDiff.Reset(listOf(FakeFfiTimelineItem()))))
        assertThat(timelineItems.value.count()).isEqualTo(1)
        assertThat(timelineItems.value).containsExactly(
            MatrixTimelineItem.Other,
        )
    }
}

internal fun TestScope.createMatrixTimelineDiffProcessor(
    timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> = MutableSharedFlow(),
    membershipChangeEventReceivedFlow: MutableSharedFlow<Unit> = MutableSharedFlow(),
    syncedEventReceivedFlow: MutableSharedFlow<Unit> = MutableSharedFlow(),
    ): MatrixTimelineDiffProcessor {
    val timelineEventContentMapper = TimelineEventContentMapper()
    val timelineItemFactory = MatrixTimelineItemMapper(
        fetchDetailsForEvent = { _ -> Result.success(Unit) },
        coroutineScope = this,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = timelineEventContentMapper
        )
    )
    return MatrixTimelineDiffProcessor(
        timelineItems = timelineItems,
        membershipChangeEventReceivedFlow = membershipChangeEventReceivedFlow,
        syncedEventReceivedFlow = syncedEventReceivedFlow,
        timelineItemMapper = timelineItemFactory,
    )
}
