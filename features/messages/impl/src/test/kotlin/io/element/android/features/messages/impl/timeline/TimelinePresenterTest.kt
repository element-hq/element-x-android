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

package io.element.android.features.messages.impl.timeline

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.FakeMessagesNavigator
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.voicemessages.timeline.FakeRedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.aRedactedMatrixTimeline
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.poll.test.actions.FakeEndPollAction
import io.element.android.features.poll.test.actions.FakeSendPollResponseAction
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.InMemorySessionPreferencesStore
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.event.Receipt
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.awaitWithLatch
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Date
import kotlin.time.Duration.Companion.seconds

private const val FAKE_UNIQUE_ID = "FAKE_UNIQUE_ID"
private const val FAKE_UNIQUE_ID_2 = "FAKE_UNIQUE_ID_2"

class TimelinePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.timelineItems).isEmpty()
            val loadedNoTimelineState = awaitItem()
            assertThat(loadedNoTimelineState.timelineItems).isEmpty()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.backPaginationStatus.hasMoreToLoadBackwards).isTrue()
            assertThat(initialState.backPaginationStatus.isBackPaginating).isFalse()
            initialState.eventSink.invoke(TimelineEvents.LoadMore)
            val inPaginationState = awaitItem()
            assertThat(inPaginationState.backPaginationStatus.isBackPaginating).isTrue()
            assertThat(inPaginationState.backPaginationStatus.hasMoreToLoadBackwards).isTrue()
            val postPaginationState = awaitItem()
            assertThat(postPaginationState.backPaginationStatus.hasMoreToLoadBackwards).isTrue()
            assertThat(postPaginationState.backPaginationStatus.isBackPaginating).isFalse()
        }
    }

    @Test
    fun `present - set highlighted event`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            skipItems(1)
            assertThat(initialState.highlightedEventId).isNull()
            initialState.eventSink.invoke(TimelineEvents.SetHighlightedEvent(AN_EVENT_ID))
            val withHighlightedState = awaitItem()
            assertThat(withHighlightedState.highlightedEventId).isEqualTo(AN_EVENT_ID)
            initialState.eventSink.invoke(TimelineEvents.SetHighlightedEvent(null))
            val withoutHighlightedState = awaitItem()
            assertThat(withoutHighlightedState.highlightedEventId).isNull()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - on scroll finished mark a room as read if the first visible index is 0`() = runTest(StandardTestDispatcher()) {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem())
            )
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = false)
        val room = FakeMatrixRoom(matrixTimeline = timeline)
        val presenter = createTimelinePresenter(
            timeline = timeline,
            room = room,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sentReadReceipts).isEmpty()
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            runCurrent()
            assertThat(room.markAsReadCalls).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished send read receipt if an event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem()),
                MatrixTimelineItem.Event(
                    uniqueId = FAKE_UNIQUE_ID_2,
                    event = anEventTimelineItem(
                        eventId = AN_EVENT_ID_2,
                        content = aMessageContent("Test message")
                    )
                )
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sentReadReceipts).isEmpty()
            val initialState = awaitFirstItem()
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sentReadReceipts).isNotEmpty()
            assertThat(timeline.sentReadReceipts.first().second).isEqualTo(ReceiptType.READ)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished send a private read receipt if an event is at an index other than 0 and public read receipts are disabled`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem()),
                MatrixTimelineItem.Event(
                    uniqueId = FAKE_UNIQUE_ID_2,
                    event = anEventTimelineItem(
                        eventId = AN_EVENT_ID_2,
                        content = aMessageContent("Test message")
                    )
                )
            )
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = false)
        val presenter = createTimelinePresenter(
            timeline = timeline,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sentReadReceipts).isEmpty()
            val initialState = awaitFirstItem()
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sentReadReceipts).isNotEmpty()
            assertThat(timeline.sentReadReceipts.first().second).isEqualTo(ReceiptType.READ_PRIVATE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt the first visible event is the same as before`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem()),
                MatrixTimelineItem.Event(
                    uniqueId = FAKE_UNIQUE_ID_2,
                    event = anEventTimelineItem(
                        eventId = AN_EVENT_ID_2,
                        content = aMessageContent("Test message")
                    )
                )
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sentReadReceipts).isEmpty()
            val initialState = awaitFirstItem()
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sentReadReceipts).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt only virtual events exist before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Virtual(FAKE_UNIQUE_ID, VirtualTimelineItem.ReadMarker),
                MatrixTimelineItem.Virtual(FAKE_UNIQUE_ID, VirtualTimelineItem.ReadMarker)
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sentReadReceipts).isEmpty()
            val initialState = awaitFirstItem()
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sentReadReceipts).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - covers newEventState scenarios`() = runTest {
        val timeline = FakeMatrixTimeline()
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            timeline.updateTimelineItems {
                listOf(MatrixTimelineItem.Event("0", anEventTimelineItem(content = aMessageContent())))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 1 }
            // Mimics sending a message, and assert newEventState is FromMe
            timeline.updateTimelineItems { items ->
                val event = anEventTimelineItem(content = aMessageContent(), isOwn = true)
                items + listOf(MatrixTimelineItem.Event("1", event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 2 }
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.FromMe)
            }
            // Mimics receiving a message without clearing the previous FromMe
            timeline.updateTimelineItems { items ->
                val event = anEventTimelineItem(content = aMessageContent())
                items + listOf(MatrixTimelineItem.Event("2", event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 3 }

            // Scroll to bottom to clear previous FromMe
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.None)
            }
            // Mimics receiving a message and assert newEventState is FromOther
            timeline.updateTimelineItems { items ->
                val event = anEventTimelineItem(content = aMessageContent())
                items + listOf(MatrixTimelineItem.Event("3", event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 4 }
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.FromOther)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - reaction ordering`() = runTest {
        val timeline = FakeMatrixTimeline()
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            val now = Date().time
            val minuteInMillis = 60 * 1000
            // Use index as a convenient value for timestamp
            val (alice, bob, charlie) = aMatrixUserList().take(3).mapIndexed { i, user ->
                ReactionSender(senderId = user.userId, timestamp = now + i * minuteInMillis)
            }
            val oneReaction = persistentListOf(
                EventReaction(
                    key = "❤️",
                    senders = persistentListOf(alice, charlie)
                ),
                EventReaction(
                    key = "👍",
                    senders = persistentListOf(alice, bob)
                ),
                EventReaction(
                    key = "🐶",
                    senders = persistentListOf(charlie)
                ),
            )
            timeline.updateTimelineItems {
                listOf(MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(reactions = oneReaction)))
            }
            skipItems(1)
            val item = awaitItem().timelineItems.first()
            assertThat(item).isInstanceOf(TimelineItem.Event::class.java)
            val event = item as TimelineItem.Event
            val reactions = event.reactionsState.reactions
            assertThat(reactions.size).isEqualTo(3)

            // Aggregated reactions are sorted by count first and then timestamp ascending(new ones tagged on the end)
            assertThat(reactions[0].count).isEqualTo(2)
            assertThat(reactions[0].key).isEqualTo("👍")
            assertThat(reactions[0].senders[0].senderId).isEqualTo(bob.senderId)

            assertThat(reactions[1].count).isEqualTo(2)
            assertThat(reactions[1].key).isEqualTo("❤️")
            assertThat(reactions[1].senders[0].senderId).isEqualTo(charlie.senderId)

            assertThat(reactions[2].count).isEqualTo(1)
            assertThat(reactions[2].key).isEqualTo("🐶")
            assertThat(reactions[2].senders[0].senderId).isEqualTo(charlie.senderId)
        }
    }

    @Test
    fun `present - PollAnswerSelected event`() = runTest {
        val sendPollResponseAction = FakeSendPollResponseAction()
        val presenter = createTimelinePresenter(
            sendPollResponseAction = sendPollResponseAction,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.PollAnswerSelected(AN_EVENT_ID, "anAnswerId"))
        }
        delay(1)
        sendPollResponseAction.verifyExecutionCount(1)
    }

    @Test
    fun `present - PollEndClicked event`() = runTest {
        val endPollAction = FakeEndPollAction()
        val presenter = createTimelinePresenter(
            endPollAction = endPollAction,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.PollEndClicked(AN_EVENT_ID))
        }
        delay(1)
        endPollAction.verifyExecutionCount(1)
    }

    @Test
    fun `present - PollEditClicked event navigates`() = runTest {
        val navigator = FakeMessagesNavigator()
        val presenter = createTimelinePresenter(
            messagesNavigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().eventSink(TimelineEvents.PollEditClicked(AN_EVENT_ID))
            assertThat(navigator.onEditPollClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - side effect on redacted items is invoked`() = runTest {
        val redactedVoiceMessageManager = FakeRedactedVoiceMessageManager()
        val presenter = createTimelinePresenter(
            timeline = FakeMatrixTimeline(
                initialTimelineItems = aRedactedMatrixTimeline(AN_EVENT_ID),
            ),
            redactedVoiceMessageManager = redactedVoiceMessageManager,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(0)
            awaitFirstItem().let {
                assertThat(it.timelineItems).isNotEmpty()
            }
            assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(1)
        }
    }

    @Test
    fun `present - when room member info is loaded, read receipts info should be updated`() = runTest {
        val timeline = FakeMatrixTimeline(
            listOf(
                MatrixTimelineItem.Event(
                    FAKE_UNIQUE_ID,
                    anEventTimelineItem(
                        sender = A_USER_ID,
                        receipts = persistentListOf(
                            Receipt(
                                userId = A_USER_ID,
                                timestamp = 0L,
                            )
                        )
                    )
                )
            )
        )
        val room = FakeMatrixRoom(matrixTimeline = timeline).apply {
            givenRoomMembersState(MatrixRoomMembersState.Unknown)
        }

        val avatarUrl = "https://domain.com/avatar.jpg"

        val presenter = createTimelinePresenter(timeline, room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate(30.seconds) { it.timelineItems.isNotEmpty() }.last()
            val event = initialState.timelineItems.first() as TimelineItem.Event
            assertThat(event.senderAvatar.url).isNull()
            assertThat(event.readReceiptState.receipts.first().avatarData.url).isNull()

            room.givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    persistentListOf(aRoomMember(userId = A_USER_ID, avatarUrl = avatarUrl))
                )
            )

            val updatedEvent = awaitItem().timelineItems.first() as TimelineItem.Event
            assertThat(updatedEvent.readReceiptState.receipts.first().avatarData.url).isEqualTo(avatarUrl)
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        // Skip 1 item if Mentions feature is enabled
        if (FeatureFlags.Mentions.defaultValue) {
            skipItems(1)
        }
        return awaitItem()
    }

    private fun TestScope.createTimelinePresenter(
        timeline: MatrixTimeline = FakeMatrixTimeline(),
        room: FakeMatrixRoom = FakeMatrixRoom(matrixTimeline = timeline),
        timelineItemsFactory: TimelineItemsFactory = aTimelineItemsFactory(),
        redactedVoiceMessageManager: RedactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
        messagesNavigator: FakeMessagesNavigator = FakeMessagesNavigator(),
        endPollAction: EndPollAction = FakeEndPollAction(),
        sendPollResponseAction: SendPollResponseAction = FakeSendPollResponseAction(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
    ): TimelinePresenter {
        return TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = room,
            dispatchers = testCoroutineDispatchers(),
            appScope = this,
            navigator = messagesNavigator,
            redactedVoiceMessageManager = redactedVoiceMessageManager,
            endPollAction = endPollAction,
            sendPollResponseAction = sendPollResponseAction,
            sessionPreferencesStore = sessionPreferencesStore,
        )
    }
}
