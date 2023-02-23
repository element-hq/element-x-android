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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.actionlist

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.actionlist.model.TimelineItemAction
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.features.messages.timeline.model.content.TimelineItemContent
import io.element.android.features.messages.timeline.model.content.TimelineItemRedactedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemTextContent
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrixtest.AN_EVENT_ID
import io.element.android.libraries.matrixtest.A_MESSAGE
import io.element.android.libraries.matrixtest.A_USER_ID
import io.element.android.libraries.matrixtest.A_USER_NAME
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ActionListPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = ActionListPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from me redacted`() = runTest {
        val presenter = ActionListPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(true, TimelineItemRedactedContent)
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from others redacted`() = runTest {
        val presenter = ActionListPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(false, TimelineItemRedactedContent)
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message`() = runTest {
        val presenter = ActionListPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message`() = runTest {
        val presenter = ActionListPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }
}

private fun aMessageEvent(
    isMine: Boolean,
    content: TimelineItemContent,
) = TimelineItem.MessageEvent(
    id = AN_EVENT_ID,
    senderId = A_USER_ID.value,
    senderDisplayName = A_USER_NAME,
    senderAvatar = AvatarData(A_USER_ID.value, A_USER_NAME),
    content = content,
    sentTime = "",
    isMine = isMine,
    reactionsState = TimelineItemReactions(persistentListOf())
)
