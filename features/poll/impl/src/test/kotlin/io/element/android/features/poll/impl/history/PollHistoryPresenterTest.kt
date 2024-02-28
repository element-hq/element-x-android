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

package io.element.android.features.poll.impl.history

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.poll.impl.aPollTimeline
import io.element.android.features.poll.impl.anEndedPollContent
import io.element.android.features.poll.impl.anOngoingPollContent
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItemsFactory
import io.element.android.features.poll.impl.model.DefaultPollContentStateFactory
import io.element.android.features.poll.test.actions.FakeEndPollAction
import io.element.android.features.poll.test.actions.FakeSendPollResponseAction
import io.element.android.libraries.dateformatter.test.FakeDaySeparatorFormatter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PollHistoryPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val timeline = aPollTimeline(
        polls = mapOf(
            AN_EVENT_ID to anOngoingPollContent(),
            AN_EVENT_ID_2 to anEndedPollContent()
        )
    )
    private val room = FakeMatrixRoom(
        matrixTimeline = timeline
    )

    @Test
    fun `present - initial states`() = runTest {
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
                assertThat(state.pollHistoryItems.size).isEqualTo(0)
                assertThat(state.isLoading).isTrue()
                assertThat(state.hasMoreToLoad).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.pollHistoryItems.size).isEqualTo(2)
                assertThat(state.pollHistoryItems.ongoing).hasSize(1)
                assertThat(state.pollHistoryItems.past).hasSize(1)
            }
        }
    }

    @Test
    fun `present - change filter scenario`() = runTest {
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
                state.eventSink(PollHistoryEvents.OnFilterSelected(PollHistoryFilter.PAST))
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.PAST)
                state.eventSink(PollHistoryEvents.OnFilterSelected(PollHistoryFilter.ONGOING))
            }
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - poll actions scenario`() = runTest {
        val sendPollResponseAction = FakeSendPollResponseAction()
        val endPollAction = FakeEndPollAction()
        val presenter = createPollHistoryPresenter(
            sendPollResponseAction = sendPollResponseAction,
            endPollAction = endPollAction
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            state.eventSink(PollHistoryEvents.PollEndClicked(AN_EVENT_ID))
            runCurrent()
            endPollAction.verifyExecutionCount(1)
            state.eventSink(PollHistoryEvents.PollAnswerSelected(AN_EVENT_ID, "answer"))
            runCurrent()
            sendPollResponseAction.verifyExecutionCount(1)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - load more scenario`() = runTest {
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.pollHistoryItems.size).isEqualTo(2)
            }
            timeline.updatePaginationState {
                copy(isBackPaginating = false)
            }
            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            loadedState.eventSink(PollHistoryEvents.LoadMore)
            awaitItem().also { state ->
                assertThat(state.isLoading).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.isLoading).isFalse()
            }
        }
    }

    private fun TestScope.createPollHistoryPresenter(
        room: MatrixRoom = FakeMatrixRoom(),
        appCoroutineScope: CoroutineScope = this,
        endPollAction: EndPollAction = FakeEndPollAction(),
        sendPollResponseAction: SendPollResponseAction = FakeSendPollResponseAction(),
        pollHistoryItemFactory: PollHistoryItemsFactory = PollHistoryItemsFactory(
            pollContentStateFactory = DefaultPollContentStateFactory(FakeMatrixClient()),
            daySeparatorFormatter = FakeDaySeparatorFormatter(),
            dispatchers = testCoroutineDispatchers(),
        ),
    ): PollHistoryPresenter {
        return PollHistoryPresenter(
            room = room,
            appCoroutineScope = appCoroutineScope,
            sendPollResponseAction = sendPollResponseAction,
            endPollAction = endPollAction,
            pollHistoryItemFactory = pollHistoryItemFactory,
        )
    }
}
