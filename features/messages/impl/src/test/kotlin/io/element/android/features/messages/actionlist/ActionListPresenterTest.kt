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

package io.element.android.features.messages.actionlist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.fixtures.aMessageEvent
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.libraries.featureflag.test.InMemoryPreferencesStore
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ActionListPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from me redacted`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(isMine = true, content = TimelineItemRedactedContent)
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from others redacted`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(isMine = false, content = TimelineItemRedactedContent)
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
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
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message cannot sent message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = false))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message and can redact`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = true, canSendMessage = true))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a media item`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = aTimelineItemImageContent(),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a state item in debug build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val stateEvent = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemStateEventContent(),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(stateEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    stateEvent,
                    persistentListOf(
                        TimelineItemAction.Copy,
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a state item in non-debuggable build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val stateEvent = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemStateEventContent(),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(stateEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    stateEvent,
                    persistentListOf(
                        TimelineItemAction.Copy,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message in non-debuggable build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message with no actions`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false)
            )
            val redactedEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemRedactedContent,
            )

            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            assertThat(awaitItem().target).isInstanceOf(ActionListState.Target.Success::class.java)

            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(redactedEvent, canRedact = false, canSendMessage = true))
            awaitItem().run {
                assertThat(target).isEqualTo(ActionListState.Target.None)
                assertThat(displayEmojiReactions).isFalse()
            }
        }
    }

    @Test
    fun `present - compute not sent message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                eventId = null, // No event id, so it's not sent yet
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false),
            )

            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.Redact,
                    )
                )
            )
            assertThat(successState.displayEmojiReactions).isFalse()
        }
    }

    @Test
    fun `present - compute for poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = aTimelineItemPollContent(),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.EndPoll,
                        TimelineItemAction.Redact,
                    )
                )
            )
            assertThat(successState.displayEmojiReactions).isTrue()
        }
    }

    @Test
    fun `present - compute for ended poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = aTimelineItemPollContent(isEnded = true),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Redact,
                    )
                )
            )
            assertThat(successState.displayEmojiReactions).isTrue()
        }
    }

    @Test
    fun `present - compute for voice message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = aTimelineItemVoiceContent(),
            )
            initialState.eventSink.invoke(ActionListEvents.ComputeForMessage(messageEvent, canRedact = false, canSendMessage = true))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    messageEvent,
                    persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Redact,
                    )
                )
            )
            assertThat(successState.displayEmojiReactions).isTrue()
        }
    }
}

private fun createActionListPresenter(isDeveloperModeEnabled: Boolean): ActionListPresenter {
    val preferencesStore = InMemoryPreferencesStore(isDeveloperModeEnabled = isDeveloperModeEnabled)
    return ActionListPresenter(preferencesStore = preferencesStore)
}

