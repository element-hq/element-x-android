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

package io.element.android.features.messages.impl.timeline.components.retrysendmenu

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RetrySendMenuPresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val room = FakeMatrixRoom()
    private val presenter = RetrySendMenuPresenter(room)

    @Test
    fun `present - handle event selected`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent()
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            assertThat(awaitItem().selectedEvent).isSameInstanceAs(selectedEvent)
        }
    }

    @Test
    fun `present - handle dismiss`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent()
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Dismiss)
            assertThat(room.cancelSendCount).isEqualTo(0)
            assertThat(room.retrySendMessageCount).isEqualTo(0)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle resend with transactionId`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = A_TRANSACTION_ID)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Retry)
            assertThat(room.cancelSendCount).isEqualTo(0)
            assertThat(room.retrySendMessageCount).isEqualTo(1)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle resend without transactionId`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = null)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Retry)
            assertThat(room.cancelSendCount).isEqualTo(0)
            assertThat(room.retrySendMessageCount).isEqualTo(0)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle resend with error`() = runTest {
        room.givenRetrySendMessageResult(Result.failure(IllegalStateException("An error")))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = A_TRANSACTION_ID)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Retry)
            assertThat(room.cancelSendCount).isEqualTo(0)
            assertThat(room.retrySendMessageCount).isEqualTo(1)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle remove failed message with transactionId`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = A_TRANSACTION_ID)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Remove)
            assertThat(room.cancelSendCount).isEqualTo(1)
            assertThat(room.retrySendMessageCount).isEqualTo(0)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle remove failed message without transactionId`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = null)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Remove)
            assertThat(room.cancelSendCount).isEqualTo(0)
            assertThat(room.retrySendMessageCount).isEqualTo(0)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    @Test
    fun `present - handle remove failed message with error`() = runTest {
        room.givenRetrySendMessageResult(Result.failure(IllegalStateException("An error")))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent(transactionId = A_TRANSACTION_ID)
            initialState.eventSink(RetrySendMenuEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(RetrySendMenuEvents.Remove)
            assertThat(room.cancelSendCount).isEqualTo(1)
            assertThat(room.retrySendMessageCount).isEqualTo(0)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }
}
