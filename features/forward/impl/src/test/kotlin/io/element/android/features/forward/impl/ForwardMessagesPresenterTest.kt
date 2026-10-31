/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.forward.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ForwardMessagesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createForwardMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.forwardAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - forward successful`() = runTest {
        val forwardEventsLambda = lambdaRecorder { _: List<EventId>, _: List<RoomId> ->
            Result.success(Unit)
        }
        val timelines = mutableListOf<FakeTimeline>()
        val createTimelineResult = lambdaRecorder { _: CreateTimelineParams ->
            Result.success(
                FakeTimeline().apply {
                    this.forwardEventsLambda = forwardEventsLambda
                }.also { timelines.add(it) }
            )
        }
        val room = FakeJoinedRoom(createTimelineResult = createTimelineResult)
        val presenter = createForwardMessagesPresenter(
            eventIds = listOf(AN_EVENT_ID, AN_EVENT_ID_2),
            fakeRoom = room,
        )
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
            createTimelineResult.assertions().isCalledExactly(2).withSequence(
                listOf(value(CreateTimelineParams.Focused(AN_EVENT_ID))),
                listOf(value(CreateTimelineParams.Focused(AN_EVENT_ID_2))),
            )
            forwardEventsLambda.assertions().isCalledExactly(2).withSequence(
                listOf(value(listOf(AN_EVENT_ID)), value(listOf(summary.roomId))),
                listOf(value(listOf(AN_EVENT_ID_2)), value(listOf(summary.roomId))),
            )
            assertThat(timelines.map { it.closeCounter }).containsExactly(1, 1)
        }
    }

    @Test
    fun `present - select a room and forward failed, then clear`() = runTest {
        val forwardEventsLambda = lambdaRecorder { _: List<EventId>, _: List<RoomId> ->
            Result.failure<Unit>(IllegalStateException("error"))
        }
        val timeline = FakeTimeline().apply {
            this.forwardEventsLambda = forwardEventsLambda
        }
        val room = FakeJoinedRoom(createTimelineResult = { Result.success(timeline) })
        val presenter = createForwardMessagesPresenter(fakeRoom = room)
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
            failedForwardState.eventSink(ForwardMessagesEvent.ClearError)
            assertThat(awaitItem().forwardAction.isUninitialized()).isTrue()
            forwardEventsLambda.assertions().isCalledOnce()
            assertThat(timeline.closeCounter).isEqualTo(1)
        }
    }
}

fun TestScope.createForwardMessagesPresenter(
    eventIds: List<EventId> = listOf(AN_EVENT_ID),
    fakeRoom: FakeJoinedRoom = FakeJoinedRoom(),
) = ForwardMessagesPresenter(
    eventIds = eventIds,
    room = fakeRoom,
    sessionCoroutineScope = this,
)
