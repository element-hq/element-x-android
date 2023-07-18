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
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aMessageContent
import io.element.android.libraries.matrix.test.room.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.tests.testutils.awaitWithLatch
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelinePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createTimelinePresenter()
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
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.paginationState.hasMoreToLoadBackwards).isTrue()
            assertThat(initialState.paginationState.isBackPaginating).isFalse()
            initialState.eventSink.invoke(TimelineEvents.LoadMore)
            val inPaginationState = awaitItem()
            assertThat(inPaginationState.paginationState.isBackPaginating).isTrue()
            assertThat(inPaginationState.paginationState.hasMoreToLoadBackwards).isTrue()
            val postPaginationState = awaitItem()
            assertThat(postPaginationState.paginationState.hasMoreToLoadBackwards).isTrue()
            assertThat(postPaginationState.paginationState.isBackPaginating).isFalse()
        }
    }

    @Test
    fun `present - set highlighted event`() = runTest {
        val presenter = createTimelinePresenter()
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
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(0, anEventTimelineItem())
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt no event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(0, anEventTimelineItem())
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt only virtual events exist before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Virtual(0, VirtualTimelineItem.ReadMarker)
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - covers hasNewItems scenarios`() = runTest {
        val timeline = FakeMatrixTimeline()
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.hasNewItems).isFalse()
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            timeline.updateTimelineItems {
                listOf(MatrixTimelineItem.Event(0, anEventTimelineItem(content = aMessageContent())))
            }
            skipItems(1)
            assertThat(awaitItem().timelineItems.size).isEqualTo(1)
            timeline.updateTimelineItems { items ->
                items + listOf(MatrixTimelineItem.Event(1, anEventTimelineItem(content = aMessageContent())))
            }
            skipItems(1)
            assertThat(awaitItem().timelineItems.size).isEqualTo(2)
            assertThat(awaitItem().hasNewItems).isTrue()
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            assertThat(awaitItem().hasNewItems).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createTimelinePresenter(
        timeline: MatrixTimeline = FakeMatrixTimeline(),
        timelineItemsFactory: TimelineItemsFactory = aTimelineItemsFactory()
    ): TimelinePresenter {
        return TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = FakeMatrixRoom(matrixTimeline = timeline),
            dispatchers = testCoroutineDispatchers(),
            appScope = this
        )
    }
}
