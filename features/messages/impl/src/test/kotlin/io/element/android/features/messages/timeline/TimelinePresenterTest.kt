/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.timeline

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.groups.TimelineItemGrouper
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelinePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            timelineItemGrouper = TimelineItemGrouper(),
            room = FakeMatrixRoom(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.timelineItems).isEmpty()
            val loadedNoTimelineState = awaitItem()
            assertThat(loadedNoTimelineState.timelineItems).isEmpty()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val presenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            timelineItemGrouper = TimelineItemGrouper(),
            room = FakeMatrixRoom(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.paginationState.canBackPaginate).isTrue()
            assertThat(initialState.paginationState.isBackPaginating).isFalse()
            initialState.eventSink.invoke(TimelineEvents.LoadMore)
            val inPaginationState = awaitItem()
            assertThat(inPaginationState.paginationState.isBackPaginating).isTrue()
            assertThat(inPaginationState.paginationState.canBackPaginate).isTrue()
            val postPaginationState = awaitItem()
            assertThat(postPaginationState.paginationState.canBackPaginate).isTrue()
            assertThat(postPaginationState.paginationState.isBackPaginating).isFalse()
        }
    }

    @Test
    fun `present - set highlighted event`() = runTest {
        val presenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            timelineItemGrouper = TimelineItemGrouper(),
            room = FakeMatrixRoom(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            assertThat(initialState.highlightedEventId).isNull()
            initialState.eventSink.invoke(TimelineEvents.SetHighlightedEvent(AN_EVENT_ID))
            val withHighlightedState = awaitItem()
            assertThat(withHighlightedState.highlightedEventId).isEqualTo(AN_EVENT_ID)
            initialState.eventSink.invoke(TimelineEvents.SetHighlightedEvent(null))
            val withoutHighlightedState = awaitItem()
            assertThat(withoutHighlightedState.highlightedEventId).isNull()
        }
    }

    @Test
    fun `present - expand and collapse grouped events`() = runTest {
        val fakeTimeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(anEventTimelineItem() /* This is a groupable event */),
                MatrixTimelineItem.Event(anEventTimelineItem() /* This is a groupable event */),
            )
        )
        val fakeRoom = FakeMatrixRoom(matrixTimeline = fakeTimeline)
        val presenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            timelineItemGrouper = TimelineItemGrouper(),
            room = fakeRoom,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            fakeTimeline.updateTimelineItems { it }
            val loadedState = awaitItem()
            val group1 = loadedState.timelineItems.first() as TimelineItem.GroupedEvents
            assertThat(group1.expanded).isFalse()
            loadedState.eventSink.invoke(TimelineEvents.ToggleExpandGroup(group1))
            val withExpandedGroup = awaitItem()
            val group2 = withExpandedGroup.timelineItems.first() as TimelineItem.GroupedEvents
            assertThat(group2.expanded).isTrue()
            withExpandedGroup.eventSink.invoke(TimelineEvents.ToggleExpandGroup(group2))
            val withCollapsedGroup = awaitItem()
            val group3 = withCollapsedGroup.timelineItems.first() as TimelineItem.GroupedEvents
            assertThat(group3.expanded).isFalse()
        }
    }
}
