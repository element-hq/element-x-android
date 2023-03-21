/*
 * Copyright (c) 2022 New Vector Ltd
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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.fixtures.aMessageEvent
import io.element.android.features.messages.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.MessagesEvents
import io.element.android.features.messages.impl.MessagesPresenter
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.textcomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.textcomposer.MessageComposerMode
import io.element.android.services.networkmonitor.test.FakeNetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MessagesPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(A_ROOM_ID)
        }
    }

    @Test
    fun `present - handle action forward`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Forward, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    @Test
    fun `present - handle action copy`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Copy, aMessageEvent()))
            // Still a TODO in the code
        }
    }

    @Test
    fun `present - handle action reply`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent()))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
        }
    }

    @Test
    fun `present - handle action edit`() = runTest {
        val presenter = createMessagePresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Edit, aMessageEvent()))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Edit::class.java)
        }
    }

    @Test
    fun `present - handle action redact`() = runTest {
        val matrixRoom = FakeMatrixRoom()
        val presenter = createMessagePresenter(matrixRoom)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Redact, aMessageEvent()))
            assertThat(matrixRoom.redactEventEventIdParam).isEqualTo(AN_EVENT_ID)
        }
    }

    private fun TestScope.createMessagePresenter(
        matrixRoom: MatrixRoom = FakeMatrixRoom()
    ): MessagesPresenter {
        val messageComposerPresenter = MessageComposerPresenter(
            appCoroutineScope = this,
            room = matrixRoom
        )

        val timelinePresenter = TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            room = matrixRoom,
        )
        val actionListPresenter = ActionListPresenter()
        return MessagesPresenter(
            room = matrixRoom,
            composerPresenter = messageComposerPresenter,
            timelinePresenter = timelinePresenter,
            actionListPresenter = actionListPresenter,
            networkMonitor = FakeNetworkMonitor(),
        )
    }
}

