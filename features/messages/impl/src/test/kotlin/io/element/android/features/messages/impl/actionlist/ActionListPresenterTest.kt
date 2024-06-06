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

package io.element.android.features.messages.impl.actionlist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.poll.api.pollcontent.aPollAnswerItemList
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = false,
                    canSendReaction = true
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = true,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
    fun `present - compute for others message and cannot send reaction`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = true,
                    canSendMessage = true,
                    canSendReaction = false
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
    fun `present - compute for my message cannot redact`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.ViewSource,
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = stateEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = stateEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = stateEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = stateEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            // val loadingState = awaitItem()
            // assertThat(loadingState.target).isEqualTo(ActionListState.Target.Loading(messageEvent))
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
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
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null)
            )
            val redactedEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemRedactedContent,
            )

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            assertThat(awaitItem().target).isInstanceOf(ActionListState.Target.Success::class.java)

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = redactedEvent,
                    canRedactOwn = false,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            awaitItem().run {
                assertThat(target).isEqualTo(ActionListState.Target.None)
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
                // No event id, so it's not sent yet
                eventId = null,
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = null),
            )

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
                        TimelineItemAction.Edit,
                        TimelineItemAction.Copy,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for editable poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = true,
                content = aTimelineItemPollContent(answerItems = aPollAnswerItemList(hasVotes = false)),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Edit,
                        TimelineItemAction.EndPoll,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for non-editable poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = false,
                content = aTimelineItemPollContent(answerItems = aPollAnswerItemList(hasVotes = true)),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.EndPoll,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Redact,
                    )
                )
            )
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
                isEditable = false,
                content = aTimelineItemPollContent(isEnded = true),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Redact,
                    )
                )
            )
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
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = true,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for call notify`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemCallNotifyContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    canRedactOwn = true,
                    canRedactOther = false,
                    canSendMessage = true,
                    canSendReaction = true,
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    displayEmojiReactions = false,
                    actions = persistentListOf(
                        TimelineItemAction.ViewSource
                    )
                )
            )
        }
    }
}

private fun createActionListPresenter(isDeveloperModeEnabled: Boolean): ActionListPresenter {
    val preferencesStore = InMemoryAppPreferencesStore(isDeveloperModeEnabled = isDeveloperModeEnabled)
    return ActionListPresenter(appPreferencesStore = preferencesStore)
}
