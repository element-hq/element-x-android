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
        val today = daySeparator(sdkId = "sdk_1", date = "Today")
        val previousDay = daySeparator(sdkId = "sdk_2", date = "Yesterday")
        val ids = listOf(newest, today, oldest, previousDay).withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("newest", "day_separator_Today_newest", "oldest", "day_separator_Yesterday_oldest").inOrder()
    }

    @Test
    fun `a day separator without an event below it is identified by its date`() {
        val ids = listOf(daySeparator(sdkId = "sdk_1", date = "Today")).withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("day_separator_Today")
    }

    @Test
    fun `when older events of the same day are paginated, the day separator gets a new id`() {
        val before = listOf(newest, daySeparator(sdkId = "sdk_1", date = "Today"))
        // The SDK keeps the separator at the top, above the paginated events
        val after = listOf(newest, older, daySeparator(sdkId = "sdk_1", date = "Today"))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("newest", "day_separator_Today_newest").inOrder()
        assertThat(idsAfter).containsExactly("newest", "older", "day_separator_Today_older").inOrder()
    }

    @Test
    fun `when newer events are added, the day separator keeps its id`() {
        val before = listOf(older, daySeparator(sdkId = "sdk_1", date = "Today"))
        val after = listOf(newest, older, daySeparator(sdkId = "sdk_1", date = "Today"))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("older", "day_separator_Today_older").inOrder()
        assertThat(idsAfter).containsExactly("newest", "older", "day_separator_Today_older").inOrder()
    }

    @Test
    fun `when the SDK reuses the id of a day separator for an older day, the separator of the previous day keeps its id`() {
        val before = listOf(newest, daySeparator(sdkId = "sdk_1", date = "Today"))
        // After a back pagination, the SDK changes the date of the previous separator and adds a new one for the previous day
        val after = listOf(newest, daySeparator(sdkId = "sdk_2", date = "Today"), older, daySeparator(sdkId = "sdk_1", date = "11 November"))
        val idsBefore = before.withVirtualItemIdsBasedOnContent().ids()
        val idsAfter = after.withVirtualItemIdsBasedOnContent().ids()
        assertThat(idsBefore).containsExactly("newest", "day_separator_Today_newest").inOrder()
        assertThat(idsAfter).containsExactly("newest", "day_separator_Today_newest", "older", "day_separator_11 November_older").inOrder()
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
        val items = listOf(newest, daySeparator(sdkId = "sdk_1", date = "Today"), daySeparator(sdkId = "sdk_2", date = "Today"))
        val ids = items.withVirtualItemIdsBasedOnContent().ids()
        assertThat(ids).containsExactly("newest", "day_separator_Today_newest", "sdk_2").inOrder()
    }

    private fun daySeparator(sdkId: String, date: String) = TimelineItem.Virtual(UniqueId(sdkId), TimelineItemDaySeparatorModel(date))

    private fun loadingIndicator(direction: Timeline.PaginationDirection) = TimelineItem.Virtual(
        id = UniqueId(if (direction == Timeline.PaginationDirection.FORWARDS) "ForwardLoadingIndicator" else "BackwardLoadingIndicator"),
        model = TimelineItemLoadingIndicatorModel(direction, timestamp = 0),
    )

    private fun List<TimelineItem>.ids() = map { it.identifier().value }
}
