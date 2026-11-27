/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import org.junit.Test

private const val ITEM_SIZE = 100

class PaginateNearLoadingIndicatorsTest {
    private val forwardModel = TimelineItemLoadingIndicatorModel(Timeline.PaginationDirection.FORWARDS, timestamp = 1)
    private val backwardModel = TimelineItemLoadingIndicatorModel(Timeline.PaginationDirection.BACKWARDS, timestamp = 1)

    // From the newest item to the oldest one: the forward loading indicator, 20 events, the backward loading indicator
    private val items = listOf(TimelineItem.Virtual(UniqueId("forward"), forwardModel)) +
        List(20) { aTimelineItemEvent().copy(id = UniqueId("event_$it")) } +
        TimelineItem.Virtual(UniqueId("backward"), backwardModel)

    @Test
    fun `loading indicators far from the viewport are not near it`() {
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = 6..15), distance = 5)).isEmpty()
    }

    @Test
    fun `loading indicators at the distance from the viewport are near it`() {
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = 5..10), distance = 5)).containsExactly(forwardModel)
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = 11..16), distance = 5)).containsExactly(backwardModel)
    }

    @Test
    fun `loading indicators in the viewport are near it`() {
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = 0..21), distance = 5)).containsExactly(forwardModel, backwardModel)
    }

    @Test
    fun `items composed out of the viewport are ignored`() {
        // The forward loading indicator is moving out of the viewport, and reports its target position
        val layoutInfo = layoutInfo(items, inViewport = 8..12, outOfViewport = listOf(0))
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo, distance = 5)).isEmpty()
    }

    @Test
    fun `nothing is returned when the layout info doesn't match the items yet`() {
        // 10 new events were added at the bottom, but the layout info is still the one of the previous items
        val newItems = items.take(1) + List(10) { aTimelineItemEvent().copy(id = UniqueId("new_$it")) } + items.drop(1)
        assertThat(newItems.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = 0..5), distance = 5)).isNull()
    }

    @Test
    fun `no loading indicators are near an empty viewport`() {
        assertThat(items.loadingIndicatorsNearViewport(layoutInfo(items, inViewport = IntRange.EMPTY), distance = 5)).isEmpty()
    }

    private fun layoutInfo(items: List<TimelineItem>, inViewport: IntRange, outOfViewport: List<Int> = emptyList()): LazyListLayoutInfo {
        val visibleItems = inViewport.mapIndexed { position, index -> ItemInfo(index, items[index].identifier(), offset = position * ITEM_SIZE) } +
            outOfViewport.map { index -> ItemInfo(index, items[index].identifier(), offset = inViewport.count() * ITEM_SIZE + ITEM_SIZE) }
        return FakeLazyListLayoutInfo(visibleItems, viewportEndOffset = inViewport.count() * ITEM_SIZE, totalItemsCount = items.size)
    }

    private data class ItemInfo(override val index: Int, override val key: Any, override val offset: Int) : LazyListItemInfo {
        override val size: Int = ITEM_SIZE
    }

    private class FakeLazyListLayoutInfo(
        override val visibleItemsInfo: List<LazyListItemInfo>,
        override val viewportEndOffset: Int,
        override val totalItemsCount: Int,
    ) : LazyListLayoutInfo {
        override val viewportStartOffset: Int = 0
    }
}
