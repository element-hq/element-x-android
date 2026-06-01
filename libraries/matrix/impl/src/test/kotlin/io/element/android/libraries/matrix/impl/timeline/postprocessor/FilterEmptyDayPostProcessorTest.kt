/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import org.junit.Test

private const val TODAY = 1_779_779_967_000
private const val YESTERDAY = TODAY - 24 * 60 * 60 * 1000
private const val DAY_BEFORE_YESTERDAY = YESTERDAY - 24 * 60 * 60 * 1000

class FilterEmptyDayPostProcessorTest {
    private val anEvent = MatrixTimelineItem.Event(
        uniqueId = UniqueId("event"),
        event = anEventTimelineItem(),
    )

    private fun aDaySeparator(timestmap: Long) = MatrixTimelineItem.Virtual(
        uniqueId = UniqueId("day_$timestmap"),
        virtual = VirtualTimelineItem.DayDivider(timestmap)
    )

    @Test
    fun `filterEmptyDaySeparators keeps day separator with events after it`() {
        val items = listOf(
            aDaySeparator(TODAY),
            anEvent,
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(aDaySeparator(TODAY))
        assertThat(result[1]).isEqualTo(anEvent)
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator with no events after it`() {
        val items = listOf(
            aDaySeparator(YESTERDAY),
            aDaySeparator(TODAY),
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators removes second day separator and keeps first when only first has events`() {
        val items = listOf(
            aDaySeparator(YESTERDAY),
            anEvent,
            aDaySeparator(TODAY),
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(aDaySeparator(YESTERDAY))
        assertThat(result[1]).isEqualTo(anEvent)
    }

    @Test
    fun `filterEmptyDaySeparators handles multiple day separators in a row with no events`() {
        val items = listOf(
            aDaySeparator(TODAY),
            aDaySeparator(YESTERDAY),
            aDaySeparator(DAY_BEFORE_YESTERDAY),
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators keeps all items when no day separators`() {
        val items = listOf(
            anEvent.copy(uniqueId = UniqueId("event2")),
            anEvent,
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator preceded by non-event virtual item`() {
        val readMarker = MatrixTimelineItem.Virtual(
            uniqueId = UniqueId("readMarker"),
            virtual = VirtualTimelineItem.ReadMarker
        )
        val items = listOf(
            aDaySeparator(TODAY),
            readMarker,
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(readMarker)
    }

    @Test
    fun `filterEmptyDaySeparators keeps day separator when non-event virtual items are between separator and event`() {
        val readMarker = MatrixTimelineItem.Virtual(
            uniqueId = UniqueId("readMarker"),
            virtual = VirtualTimelineItem.ReadMarker
        )
        val items = listOf(
            aDaySeparator(TODAY),
            readMarker,
            anEvent,
        )
        val result = FilterEmptyDayPostProcessor().process(items)
        assertThat(result).hasSize(3)
        assertThat(result[0]).isEqualTo(aDaySeparator(TODAY))
    }
}
