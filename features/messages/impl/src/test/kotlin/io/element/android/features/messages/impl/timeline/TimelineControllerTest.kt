/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelineControllerTest {
    @Test
    fun `test switching between live and detached timeline`() = runTest {
        val liveTimeline = FakeTimeline(name = "live")
        val detachedTimeline = FakeTimeline(name = "detached")
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) }
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            assertThat(sut.isLive().first()).isTrue()
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline)
            }
            assertThat(sut.isLive().first()).isFalse()
            assertThat(detachedTimeline.closeCounter).isEqualTo(0)
            sut.focusOnLive()
            assertThat(sut.isLive().first()).isTrue()
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            assertThat(detachedTimeline.closeCounter).isEqualTo(1)
        }
    }

    @Test
    fun `test switching between detached 1 and detached 2 should close detached 1`() = runTest {
        val liveTimeline = FakeTimeline(name = "live")
        val detachedTimeline1 = FakeTimeline(name = "detached 1")
        val detachedTimeline2 = FakeTimeline(name = "detached 2")
        var callNumber = 0
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = {
                callNumber++
                when (callNumber) {
                    1 -> Result.success(detachedTimeline1)
                    2 -> Result.success(detachedTimeline2)
                    else -> lambdaError()
                }
            }
        )
        val sut = TimelineController(joinedRoom, liveTimeline)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline1)
            }
            assertThat(detachedTimeline1.closeCounter).isEqualTo(0)
            assertThat(detachedTimeline2.closeCounter).isEqualTo(0)
            // Focus on another event should close the previous detached timeline
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline2)
            }
            assertThat(detachedTimeline1.closeCounter).isEqualTo(1)
            assertThat(detachedTimeline2.closeCounter).isEqualTo(0)
        }
    }

    @Test
    fun `test switching to live when already in live should have no effect`() = runTest {
        val liveTimeline = FakeTimeline(name = "live")
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)
        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            assertThat(sut.isLive().first()).isTrue()
            sut.focusOnLive()
            assertThat(sut.isLive().first()).isTrue()
        }
    }

    @Test
    fun `test closing the TimelineController should close the detached timeline`() = runTest {
        val liveTimeline = FakeTimeline(name = "live")
        val detachedTimeline = FakeTimeline(name = "detached")
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) }
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)
        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline)
            }
            assertThat(detachedTimeline.closeCounter).isEqualTo(0)
            sut.close()
            assertThat(detachedTimeline.closeCounter).isEqualTo(1)
        }
    }

    @Test
    fun `test getting timeline item`() = runTest {
        val liveTimeline = FakeTimeline(
            name = "live",
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
                )
            )
        )
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)
        assertThat(sut.timelineItems().first()).hasSize(1)
    }

    @Test
    fun `test invokeOnCurrentTimeline use the detached timeline and not the live timeline`() = runTest {
        val lambdaForDetached = lambdaRecorder { _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val lambdaForLive = lambdaRecorder(ensureNeverCalled = true) { _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val liveTimeline = FakeTimeline(name = "live").apply {
            sendMessageLambda = lambdaForLive
        }
        val detachedTimeline = FakeTimeline(name = "detached").apply {
            sendMessageLambda = lambdaForDetached
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) }
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)
        sut.activeTimelineFlow().test {
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline)
            }
            sut.invokeOnCurrentTimeline {
                sendMessage("body", "htmlBody", emptyList())
            }
            lambdaForDetached.assertions().isCalledOnce()
        }
    }

    @Test
    fun `test last forward pagination on a detached timeline should switch to live timeline`() = runTest {
        val liveTimeline = FakeTimeline(name = "live")
        val detachedTimeline = FakeTimeline(name = "detached")
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) }
        )
        val sut = TimelineController(room = joinedRoom, liveTimeline = liveTimeline)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID, null)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline)
            }
            val paginateLambda = lambdaRecorder { _: Timeline.PaginationDirection ->
                Result.success(true)
            }
            detachedTimeline.apply {
                this.paginateLambda = paginateLambda
            }
            sut.paginate(Timeline.PaginationDirection.FORWARDS)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
        }
    }
}

internal fun createTimelineController(
    room: FakeJoinedRoom = FakeJoinedRoom(liveTimeline = FakeTimeline()),
    liveTimeline: Timeline = FakeTimeline(name = "live"),
): TimelineController {
    return TimelineController(
        room = room,
        liveTimeline = liveTimeline
    )
}
