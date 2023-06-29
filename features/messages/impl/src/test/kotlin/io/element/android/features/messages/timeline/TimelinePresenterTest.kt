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
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
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
    fun `present - on scroll finished send read receipt if an event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline()
        val timelineItemsFactory = aTimelineItemsFactory().apply {
            replaceWith(listOf(MatrixTimelineItem.Event(anEventTimelineItem())))
        }
        val room = FakeMatrixRoom(matrixTimeline = timeline)
        val presenter = TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = room,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()

            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))

            assertThat(timeline.sendReadReceiptCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt no event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline()
        val timelineItemsFactory = aTimelineItemsFactory().apply {
            replaceWith(listOf(MatrixTimelineItem.Event(anEventTimelineItem())))
        }
        val room = FakeMatrixRoom(matrixTimeline = timeline)
        val presenter = TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = room,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()

            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))

            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt only virtual events exist before the index`() = runTest {
        val timeline = FakeMatrixTimeline()
        val timelineItemsFactory = aTimelineItemsFactory().apply {
            replaceWith(listOf(MatrixTimelineItem.Virtual(VirtualTimelineItem.ReadMarker)))
        }
        val room = FakeMatrixRoom(matrixTimeline = timeline)
        val presenter = TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = room,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()

            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))

            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
        }
    }
}
