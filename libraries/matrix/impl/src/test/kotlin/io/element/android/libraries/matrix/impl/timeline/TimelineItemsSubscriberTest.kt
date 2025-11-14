/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustEventTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimeline
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineDiff
import uniffi.matrix_sdk_ui.EventItemOrigin

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineItemsSubscriberTest {
    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `when timeline emits an empty list of items, the flow must emits an empty list`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeFfiTimeline()
        val diffProcessor = createMatrixTimelineDiffProcessor(
            timelineItems = timelineItems,
        )
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            timeline = timeline,
            timelineDiffProcessor = diffProcessor,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(listOf(TimelineDiff.Reset(emptyList())))
            val final = awaitItem()
            assertThat(final).isEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `when timeline emits a non empty list of items, the flow must emits a non empty list`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeFfiTimeline()
        val diffProcessor = createMatrixTimelineDiffProcessor(
            timelineItems = timelineItems,
        )
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            timeline = timeline,
            timelineDiffProcessor = diffProcessor,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(listOf(TimelineDiff.Reset(listOf(FakeFfiTimelineItem()))))
            val final = awaitItem()
            assertThat(final).isNotEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `when timeline emits an item with SYNC origin`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeFfiTimeline()
        val diffProcessor = createMatrixTimelineDiffProcessor(
            timelineItems = timelineItems,
        )
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            timeline = timeline,
            timelineDiffProcessor = diffProcessor,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(
                listOf(
                    TimelineDiff.Reset(
                        listOf(FakeFfiTimelineItem(
                            asEventResult = aRustEventTimelineItem(origin = EventItemOrigin.SYNC),
                        ))
                    )
                )
            )
            val final = awaitItem()
            assertThat(final).isNotEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `multiple subscriptions does not have side effect`() = runTest {
        val timelineItemsSubscriber = createTimelineItemsSubscriber()
        timelineItemsSubscriber.subscribeIfNeeded()
        timelineItemsSubscriber.subscribeIfNeeded()
        timelineItemsSubscriber.unsubscribeIfNeeded()
        timelineItemsSubscriber.unsubscribeIfNeeded()
    }
}

private fun TestScope.createTimelineItemsSubscriber(
    timeline: Timeline = FakeFfiTimeline(),
    timelineDiffProcessor: MatrixTimelineDiffProcessor = createMatrixTimelineDiffProcessor(),
): TimelineItemsSubscriber {
    return TimelineItemsSubscriber(
        timelineCoroutineScope = backgroundScope,
        dispatcher = StandardTestDispatcher(testScheduler),
        timeline = timeline,
        timelineDiffProcessor = timelineDiffProcessor,
    )
}
