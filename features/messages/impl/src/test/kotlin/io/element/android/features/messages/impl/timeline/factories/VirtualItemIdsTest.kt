/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import org.junit.Test

class VirtualItemIdsTest {
    private val newest = aTimelineItemEvent().copy(id = UniqueId("newest"))
    private val oldest = aTimelineItemEvent().copy(id = UniqueId("oldest"))
    private val older = aTimelineItemEvent().copy(id = UniqueId("older"))

    @Test
    fun `day separators are identified by their date and the event below them`() {
        val today = daySeparator(sdkId = "sdk_1", date = 1L)
        val previousDay = daySeparator(sdkId = "sdk_2", date = 0L)
        val ids = listOf(newest, today, oldest, previousDay).withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("newest", "day_separator_1_newest", "oldest", "day_separator_0_oldest").inOrder()
    }

    @Test
    fun `a day separator without an event below it is identified by its date`() {
        val ids = listOf(daySeparator(sdkId = "sdk_1", date = 1L)).withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("day_separator_1")
    }

    @Test
    fun `when older events of the same day are paginated, the day separator gets a new id`() {
        val before = listOf(newest, daySeparator(sdkId = "sdk_1", date = 1L))
        // The SDK keeps the separator at the top, above the paginated events
        val after = listOf(newest, older, daySeparator(sdkId = "sdk_1", date = 1L))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("newest", "day_separator_1_newest").inOrder()
        assertThat(idsAfter).containsExactly("newest", "older", "day_separator_1_older").inOrder()
    }

    @Test
    fun `when newer events are added, the day separator keeps its id`() {
        val before = listOf(older, daySeparator(sdkId = "sdk_1", date = 1L))
        val after = listOf(newest, older, daySeparator(sdkId = "sdk_1", date = 1L))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("older", "day_separator_1_older").inOrder()
        assertThat(idsAfter).containsExactly("newest", "older", "day_separator_1_older").inOrder()
    }

    @Test
    fun `when the SDK reuses the id of a day separator for an older day, the separator of the previous day keeps its id`() {
        val before = listOf(newest, daySeparator(sdkId = "sdk_1", date = 1L))
        // After a back pagination, the SDK changes the date of the previous separator and adds a new one for the previous day
        val after = listOf(newest, daySeparator(sdkId = "sdk_2", date = 1L), older, daySeparator(sdkId = "sdk_1", date = 2L))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("newest", "day_separator_1_newest").inOrder()
        assertThat(idsAfter).containsExactly("newest", "day_separator_1_newest", "older", "day_separator_2_older").inOrder()
    }

    @Test
    fun `loading indicators are identified by the event they are next to`() {
        val items = listOf(
            loadingIndicator(Timeline.PaginationDirection.FORWARDS),
            newest,
            oldest,
            loadingIndicator(Timeline.PaginationDirection.BACKWARDS),
        )
        val ids = items.withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly(
            "ForwardLoadingIndicator_newest",
            "newest",
            "oldest",
            "BackwardLoadingIndicator_oldest",
        ).inOrder()
    }

    @Test
    fun `loading indicators keep their id when there is no event`() {
        val items = listOf(
            loadingIndicator(Timeline.PaginationDirection.FORWARDS),
            loadingIndicator(Timeline.PaginationDirection.BACKWARDS),
        )
        val ids = items.withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("ForwardLoadingIndicator", "BackwardLoadingIndicator").inOrder()
    }

    @Test
    fun `other virtual items keep their id`() {
        val readMarker = TimelineItem.Virtual(UniqueId("read_marker"), TimelineItemReadMarkerModel)
        val ids = listOf(newest, readMarker, oldest).withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("newest", "read_marker", "oldest").inOrder()
    }

    @Test
    fun `day separators with the same date above the same event keep their original id, so ids are unique`() {
        val items = listOf(newest, daySeparator(sdkId = "sdk_1", date = 1L), daySeparator(sdkId = "sdk_2", date = 1L))
        val ids = items.withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("newest", "day_separator_1_newest", "sdk_2").inOrder()
    }

    private fun daySeparator(sdkId: String, date: Long) = TimelineItem.Virtual(
        UniqueId(sdkId),
        TimelineItemDaySeparatorModel(timestamp = date, formattedDate = "noop")
    )

    private fun loadingIndicator(direction: Timeline.PaginationDirection) = TimelineItem.Virtual(
        id = UniqueId(if (direction == Timeline.PaginationDirection.FORWARDS) "ForwardLoadingIndicator" else "BackwardLoadingIndicator"),
        model = TimelineItemLoadingIndicatorModel(direction, timestamp = 0),
    )

    private fun List<TimelineItem>.ids() = map { it.identifier().value }
}
