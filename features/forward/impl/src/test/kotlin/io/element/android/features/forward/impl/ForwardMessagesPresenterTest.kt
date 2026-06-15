/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.forward.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.LiveTimelineProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
        val forwardEventLambda = lambdaRecorder { _: EventId, _: List<RoomId> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.forwardEventLambda = forwardEventLambda
        }
        val room = FakeJoinedRoom(liveTimeline = timeline)
        val presenter = createForwardMessagesPresenter(fakeRoom = room)
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
        val room = FakeJoinedRoom(liveTimeline = timeline)
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
            failedForwardState.eventSink(ForwardMessagesEvents.ClearError)
            assertThat(awaitItem().forwardAction.isUninitialized()).isTrue()
            forwardEventLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - forward multiple events all succeed`() = runTest {
        val eventIds = listOf(EventId("\$e1"), EventId("\$e2"), EventId("\$e3"))
        val forwardEventLambda = lambdaRecorder { _: EventId, _: List<RoomId> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply { this.forwardEventLambda = forwardEventLambda }
        val room = FakeJoinedRoom(liveTimeline = timeline)
        val presenter = createForwardMessagesPresenter(eventIds = eventIds, fakeRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummary()
            presenter.onRoomSelected(listOf(summary.roomId))
            assertThat(awaitItem().forwardAction.isLoading()).isTrue()
            advanceUntilIdle()
            val successState = awaitItem()
            assertThat(successState.forwardAction).isEqualTo(AsyncAction.Success(listOf(summary.roomId)))
            forwardEventLambda.assertions().isCalledExactly(eventIds.size)
        }
    }

    @Test
    fun `present - forward multiple events with partial failure still succeeds`() = runTest {
        val eventIds = listOf(EventId("\$ok1"), EventId("\$fail"), EventId("\$ok2"))
        val forwardEventLambda = lambdaRecorder { eventId: EventId, _: List<RoomId> ->
            if (eventId == EventId("\$fail")) Result.failure(IllegalStateException("boom")) else Result.success(Unit)
        }
        val timeline = FakeTimeline().apply { this.forwardEventLambda = forwardEventLambda }
        val room = FakeJoinedRoom(liveTimeline = timeline)
        val presenter = createForwardMessagesPresenter(eventIds = eventIds, fakeRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummary()
            presenter.onRoomSelected(listOf(summary.roomId))
            assertThat(awaitItem().forwardAction.isLoading()).isTrue()
            advanceUntilIdle()
            // Some failed, some succeeded: no throw, the screen still closes with Success.
            val finalState = awaitItem()
            assertThat(finalState.forwardAction).isEqualTo(AsyncAction.Success(listOf(summary.roomId)))
        }
    }

    @Test
    fun `present - forward multiple events all fail surfaces failure`() = runTest {
        val eventIds = listOf(EventId("\$f1"), EventId("\$f2"))
        val forwardEventLambda = lambdaRecorder { _: EventId, _: List<RoomId> ->
            Result.failure<Unit>(IllegalStateException("boom"))
        }
        val timeline = FakeTimeline().apply { this.forwardEventLambda = forwardEventLambda }
        val room = FakeJoinedRoom(liveTimeline = timeline)
        val presenter = createForwardMessagesPresenter(eventIds = eventIds, fakeRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummary()
            presenter.onRoomSelected(listOf(summary.roomId))
            assertThat(awaitItem().forwardAction.isLoading()).isTrue()
            advanceUntilIdle()
            val finalState = awaitItem()
            assertThat(finalState.forwardAction.isFailure()).isTrue()
        }
    }
}

fun TestScope.createForwardMessagesPresenter(
    eventIds: List<EventId> = listOf(AN_EVENT_ID),
    fakeRoom: FakeJoinedRoom = FakeJoinedRoom(),
) = ForwardMessagesPresenter(
    eventIds = eventIds.map { it.value },
    timelineProvider = LiveTimelineProvider(fakeRoom),
    sessionCoroutineScope = this,
)
