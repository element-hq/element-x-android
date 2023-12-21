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

package io.element.android.features.messages.impl.forward

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetails
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ForwardMessagesPresenterTests {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isForwarding).isFalse()
            assertThat(initialState.error).isNull()
            assertThat(initialState.forwardingSucceeded).isNull()
        }
    }

    @Test
    fun `present - forward successful`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val summary = aRoomSummaryDetails()
            presenter.onRoomSelected(listOf(summary.roomId))
            val forwardingState = awaitItem()
            assertThat(forwardingState.isForwarding).isTrue()
            val successfulForwardState = awaitItem()
            assertThat(successfulForwardState.isForwarding).isFalse()
            assertThat(successfulForwardState.forwardingSucceeded).isNotNull()
        }
    }

    @Test
    fun `present - select a room and forward failed, then clear`() = runTest {
        val room = FakeMatrixRoom()
        val presenter = aPresenter(fakeMatrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Test failed forwarding
            room.givenForwardEventResult(Result.failure(Throwable("error")))
            skipItems(1)
            val summary = aRoomSummaryDetails()
            presenter.onRoomSelected(listOf(summary.roomId))
            skipItems(1)
            val failedForwardState = awaitItem()
            assertThat(failedForwardState.error).isNotNull()
            // Then clear error
            failedForwardState.eventSink(ForwardMessagesEvents.ClearError)
            assertThat(awaitItem().error).isNull()
        }
    }

    private fun CoroutineScope.aPresenter(
        eventId: EventId = AN_EVENT_ID,
        fakeMatrixRoom: FakeMatrixRoom = FakeMatrixRoom(),
        coroutineScope: CoroutineScope = this,
    ) = ForwardMessagesPresenter(
        eventId = eventId.value,
        room = fakeMatrixRoom,
        matrixCoroutineScope = coroutineScope,
    )
}
