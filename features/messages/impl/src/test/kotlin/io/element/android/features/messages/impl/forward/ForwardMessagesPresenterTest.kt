/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.forward

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.LiveTimelineProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ForwardMessagesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = aForwardMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.forwardAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - forward successful`() = runTest {
        val forwardEventLambda = lambdaRecorder { _: EventId, _: List<RoomId> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.forwardEventLambda = forwardEventLambda
        }
        val room = FakeMatrixRoom(liveTimeline = timeline)
        val presenter = aForwardMessagesPresenter(fakeMatrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummary()
            presenter.onRoomSelected(listOf(summary.roomId))
            val forwardingState = awaitItem()
            assertThat(forwardingState.forwardAction.isLoading()).isTrue()
            val successfulForwardState = awaitItem()
            assertThat(successfulForwardState.forwardAction).isEqualTo(AsyncAction.Success(listOf(summary.roomId)))
            forwardEventLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - select a room and forward failed, then clear`() = runTest {
        val forwardEventLambda = lambdaRecorder { _: EventId, _: List<RoomId> ->
            Result.failure<Unit>(IllegalStateException("error"))
        }
        val timeline = FakeTimeline().apply {
            this.forwardEventLambda = forwardEventLambda
        }
        val room = FakeMatrixRoom(liveTimeline = timeline)
        val presenter = aForwardMessagesPresenter(fakeMatrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummary()
            presenter.onRoomSelected(listOf(summary.roomId))
            skipItems(1)
            val failedForwardState = awaitItem()
            assertThat(failedForwardState.forwardAction.isFailure()).isTrue()
            // Then clear error
            failedForwardState.eventSink(ForwardMessagesEvents.ClearError)
            assertThat(awaitItem().forwardAction.isUninitialized()).isTrue()
            forwardEventLambda.assertions().isCalledOnce()
        }
    }

    private fun CoroutineScope.aForwardMessagesPresenter(
        eventId: EventId = AN_EVENT_ID,
        fakeMatrixRoom: FakeMatrixRoom = FakeMatrixRoom(),
        coroutineScope: CoroutineScope = this,
    ) = ForwardMessagesPresenter(
        eventId = eventId.value,
        timelineProvider = LiveTimelineProvider(fakeMatrixRoom),
        appCoroutineScope = coroutineScope,
    )
}
