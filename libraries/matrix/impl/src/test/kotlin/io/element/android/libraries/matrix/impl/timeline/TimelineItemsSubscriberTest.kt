/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustEventTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustTimeline
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustTimelineDiff
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustTimelineItem
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineChange
import uniffi.matrix_sdk_ui.EventItemOrigin

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineItemsSubscriberTest {
    @Test
    fun `when timeline emits an empty list of items, the flow must emits an empty list`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeRustTimeline()
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            coroutineScope = backgroundScope,
            timeline = timeline,
            timelineItems = timelineItems,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(listOf(FakeRustTimelineDiff(item = null, change = TimelineChange.RESET)))
            val final = awaitItem()
            assertThat(final).isEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
    }

    @Test
    fun `when timeline emits a non empty list of items, the flow must emits a non empty list`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeRustTimeline()
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            coroutineScope = backgroundScope,
            timeline = timeline,
            timelineItems = timelineItems,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(listOf(FakeRustTimelineDiff(item = FakeRustTimelineItem(), change = TimelineChange.RESET)))
            val final = awaitItem()
            assertThat(final).isNotEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
    }

    @Test
    fun `when timeline emits an item with SYNC origin, the callback onNewSyncedEvent is invoked`() = runTest {
        val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
            MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
        val timeline = FakeRustTimeline()
        val onNewSyncedEventRecorder = lambdaRecorder<Unit> { }
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            coroutineScope = backgroundScope,
            timeline = timeline,
            timelineItems = timelineItems,
            onNewSyncedEvent = onNewSyncedEventRecorder,
        )
        timelineItems.test {
            timelineItemsSubscriber.subscribeIfNeeded()
            // Wait for the listener to be set.
            runCurrent()
            timeline.emitDiff(
                listOf(
                    FakeRustTimelineDiff(
                        item = FakeRustTimelineItem(
                            asEventResult = FakeRustEventTimelineItem(origin = EventItemOrigin.SYNC)
                        ),
                        change = TimelineChange.RESET,
                    )
                )
            )
            val final = awaitItem()
            assertThat(final).isNotEmpty()
            timelineItemsSubscriber.unsubscribeIfNeeded()
        }
        onNewSyncedEventRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `multiple subscriptions does not have side effect`() = runTest {
        val timelineItemsSubscriber = createTimelineItemsSubscriber(
            coroutineScope = backgroundScope,
        )
        timelineItemsSubscriber.subscribeIfNeeded()
        timelineItemsSubscriber.subscribeIfNeeded()
        timelineItemsSubscriber.unsubscribeIfNeeded()
        timelineItemsSubscriber.unsubscribeIfNeeded()
    }
}

private fun TestScope.createTimelineItemsSubscriber(
    coroutineScope: CoroutineScope,
    timeline: Timeline = FakeRustTimeline(),
    timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> = MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE),
    initLatch: CompletableDeferred<Unit> = CompletableDeferred(),
    isTimelineInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false),
    onNewSyncedEvent: () -> Unit = { lambdaError() },
): TimelineItemsSubscriber {
    return TimelineItemsSubscriber(
        timelineCoroutineScope = coroutineScope,
        dispatcher = StandardTestDispatcher(testScheduler),
        timeline = timeline,
        timelineDiffProcessor = createMatrixTimelineDiffProcessor(timelineItems),
        initLatch = initLatch,
        isTimelineInitialized = isTimelineInitialized,
        onNewSyncedEvent = onNewSyncedEvent,
    )
}
