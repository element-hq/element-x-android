/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline))
        val sut = TimelineController(matrixRoom)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            assertThat(sut.isLive().first()).isTrue()
            sut.focusOnEvent(AN_EVENT_ID)
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val sut = TimelineController(matrixRoom)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline1))
            sut.focusOnEvent(AN_EVENT_ID)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(detachedTimeline1)
            }
            assertThat(detachedTimeline1.closeCounter).isEqualTo(0)
            assertThat(detachedTimeline2.closeCounter).isEqualTo(0)
            // Focus on another event should close the previous detached timeline
            matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline2))
            sut.focusOnEvent(AN_EVENT_ID)
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val sut = TimelineController(matrixRoom)
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline))
        val sut = TimelineController(matrixRoom)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID)
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val sut = TimelineController(matrixRoom)
        assertThat(sut.timelineItems().first()).hasSize(1)
    }

    @Test
    fun `test invokeOnCurrentTimeline use the detached timeline and not the live timeline`() = runTest {
        val lambdaForDetached = lambdaRecorder { _: String, _: String?, _: List<Mention> ->
            Result.success(Unit)
        }
        val lambdaForLive = lambdaRecorder(ensureNeverCalled = true) { _: String, _: String?, _: List<Mention> ->
            Result.success(Unit)
        }
        val liveTimeline = FakeTimeline(name = "live").apply {
            sendMessageLambda = lambdaForLive
        }
        val detachedTimeline = FakeTimeline(name = "detached").apply {
            sendMessageLambda = lambdaForDetached
        }
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline))
        val sut = TimelineController(matrixRoom)
        sut.activeTimelineFlow().test {
            sut.focusOnEvent(AN_EVENT_ID)
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID)
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
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        matrixRoom.givenTimelineFocusedOnEventResult(Result.success(detachedTimeline))
        val sut = TimelineController(matrixRoom)

        sut.activeTimelineFlow().test {
            awaitItem().also { state ->
                assertThat(state).isEqualTo(liveTimeline)
            }
            sut.focusOnEvent(AN_EVENT_ID)
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
