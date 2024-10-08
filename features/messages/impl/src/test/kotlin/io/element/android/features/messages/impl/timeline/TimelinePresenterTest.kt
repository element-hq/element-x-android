/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.FakeMessagesNavigator
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.aResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactoryCreator
import io.element.android.features.messages.impl.timeline.components.aCriticalShield
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.features.messages.impl.voicemessages.timeline.FakeRedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.aRedactedMatrixTimeline
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.poll.test.actions.FakeEndPollAction
import io.element.android.features.poll.test.actions.FakeSendPollResponseAction
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.event.Receipt
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class) class TimelinePresenterTest {
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
            assertThat(initialState.isLive).isTrue()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.focusedEventId).isNull()
            assertThat(initialState.focusRequestState).isEqualTo(FocusRequestState.None)
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val paginateLambda = lambdaRecorder { _: Timeline.PaginationDirection ->
            Result.success(false)
        }
        val timeline = FakeTimeline().apply {
            this.paginateLambda = paginateLambda
        }
        val presenter = createTimelinePresenter(timeline = timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(TimelineEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS))
            initialState.eventSink.invoke(TimelineEvents.LoadMore(Timeline.PaginationDirection.FORWARDS))
            assert(paginateLambda)
                .isCalledExactly(2)
                .withSequence(
                    listOf(value(Timeline.PaginationDirection.BACKWARDS)),
                    listOf(value(Timeline.PaginationDirection.FORWARDS))
                )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - on scroll finished mark a room as read if the first visible index is 0`() = runTest(StandardTestDispatcher()) {
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
                )
            )
        )
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = false)
        val presenter = createTimelinePresenter(
            timeline = timeline,
            room = room,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            runCurrent()
            assertThat(room.markAsReadCalls).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished send read receipt if an event is before the index`() = runTest {
        val sendReadReceiptsLambda = lambdaRecorder { _: EventId, _: ReceiptType ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem()),
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID_2,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID_2,
                            content = aMessageContent("Test message")
                        )
                    )
                )
            )
        ).apply {
            this.sendReadReceiptLambda = sendReadReceiptsLambda
        }
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().run {
                eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            advanceUntilIdle()
            assert(sendReadReceiptsLambda)
                .isCalledOnce()
                .with(any(), value(ReceiptType.READ))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished send a private read receipt if an event is at an index other than 0 and public read receipts are disabled`() = runTest {
        val sendReadReceiptsLambda = lambdaRecorder { _: EventId, _: ReceiptType ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem()),
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID_2,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID_2,
                            content = aMessageContent("Test message")
                        )
                    )
                )
            )
        ).apply {
            this.sendReadReceiptLambda = sendReadReceiptsLambda
        }
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = false)
        val presenter = createTimelinePresenter(
            timeline = timeline,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().run {
                eventSink.invoke(TimelineEvents.OnScrollFinished(0))
                eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            advanceUntilIdle()
            assert(sendReadReceiptsLambda)
                .isCalledOnce()
                .with(any(), value(ReceiptType.READ_PRIVATE))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt the first visible event is the same as before`() = runTest {
        val sendReadReceiptsLambda = lambdaRecorder { _: EventId, _: ReceiptType ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem()),
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID_2,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID_2,
                            content = aMessageContent("Test message")
                        )
                    )
                )
            )
        ).apply {
            this.sendReadReceiptLambda = sendReadReceiptsLambda
        }
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().run {
                eventSink.invoke(TimelineEvents.OnScrollFinished(1))
                eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
            assert(sendReadReceiptsLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt only virtual events exist before the index`() = runTest {
        val sendReadReceiptsLambda = lambdaRecorder { _: EventId, _: ReceiptType ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Virtual(A_UNIQUE_ID, VirtualTimelineItem.ReadMarker),
                    MatrixTimelineItem.Virtual(A_UNIQUE_ID, VirtualTimelineItem.ReadMarker)
                )
            )
        ).apply {
            this.sendReadReceiptLambda = sendReadReceiptsLambda
        }
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            cancelAndIgnoreRemainingEvents()
            assert(sendReadReceiptsLambda).isNeverCalled()
        }
    }

    @Test
    fun `present - covers newEventState scenarios`() = runTest {
        val timelineItems = MutableStateFlow(emptyList<MatrixTimelineItem>())
        val timeline = FakeTimeline(timelineItems = timelineItems)
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            timelineItems.emit(
                listOf(MatrixTimelineItem.Event(UniqueId("0"), anEventTimelineItem(content = aMessageContent())))
            )
            consumeItemsUntilPredicate { it.timelineItems.size == 1 }
            // Mimics sending a message, and assert newEventState is FromMe
            timelineItems.getAndUpdate { items ->
                val event = anEventTimelineItem(content = aMessageContent(), isOwn = true)
                items + listOf(MatrixTimelineItem.Event(UniqueId("1"), event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 2 }
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.FromMe)
            }
            // Mimics receiving a message without clearing the previous FromMe
            timelineItems.getAndUpdate { items ->
                val event = anEventTimelineItem(content = aMessageContent())
                items + listOf(MatrixTimelineItem.Event(UniqueId("2"), event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 3 }

            // Scroll to bottom to clear previous FromMe
            initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.None)
            }
            // Mimics receiving a message and assert newEventState is FromOther
            timelineItems.getAndUpdate { items ->
                val event = anEventTimelineItem(content = aMessageContent())
                items + listOf(MatrixTimelineItem.Event(UniqueId("3"), event))
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
        val timelineItems = MutableStateFlow(emptyList<MatrixTimelineItem>())
        val timeline = FakeTimeline(
            timelineItems = timelineItems,
        )
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
            timelineItems.emit(
                listOf(MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(reactions = oneReaction)))
            )
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
            initialState.eventSink.invoke(TimelineEvents.SelectPollAnswer(AN_EVENT_ID, "anAnswerId"))
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
            initialState.eventSink.invoke(TimelineEvents.EndPoll(AN_EVENT_ID))
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
            awaitFirstItem().eventSink(TimelineEvents.EditPoll(AN_EVENT_ID))
            assertThat(navigator.onEditPollClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - side effect on redacted items is invoked`() = runTest {
        val redactedVoiceMessageManager = FakeRedactedVoiceMessageManager()
        val presenter = createTimelinePresenter(
            timeline = FakeTimeline(
                timelineItems = flowOf(
                    aRedactedMatrixTimeline(AN_EVENT_ID),
                )
            ),
            redactedVoiceMessageManager = redactedVoiceMessageManager,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(0)
            skipItems(2)
            assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(1)
        }
    }

    @Test
    fun `present - focus on event and jump to live make the presenter update the state with the correct Events`() = runTest {
        val detachedTimeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID,
                        event = anEventTimelineItem(),
                    )
                )
            )
        )
        val liveTimeline = FakeTimeline(
            timelineItems = flowOf(emptyList())
        )
        val room = FakeMatrixRoom(
            liveTimeline = liveTimeline,
            timelineFocusedOnEventResult = { Result.success(detachedTimeline) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
        )
        val presenter = createTimelinePresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.FocusOnEvent(AN_EVENT_ID))
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Requested(AN_EVENT_ID, Duration.ZERO))
            }
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Success(AN_EVENT_ID))
                assertThat(state.timelineItems).isNotEmpty()
            }
            initialState.eventSink.invoke(TimelineEvents.JumpToLive)
            skipItems(2)
            awaitItem().also { state ->
                // Event stays focused
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.timelineItems).isEmpty()
            }
        }
    }

    @Test
    fun `present - focus on known event retrieves the event from cache`() = runTest {
        val timelineItemIndexer = TimelineItemIndexer().apply {
            process(listOf(aMessageEvent(eventId = AN_EVENT_ID)))
        }
        val presenter = createTimelinePresenter(
            room = FakeMatrixRoom(
                liveTimeline = FakeTimeline(
                    timelineItems = flowOf(
                        listOf(
                            MatrixTimelineItem.Event(
                                uniqueId = A_UNIQUE_ID,
                                event = anEventTimelineItem(eventId = AN_EVENT_ID),
                            )
                        )
                    )
                ),
                canUserSendMessageResult = { _, _ -> Result.success(true) },
            ),
            timelineItemIndexer = timelineItemIndexer,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(TimelineEvents.FocusOnEvent(AN_EVENT_ID))
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Requested(AN_EVENT_ID, Duration.ZERO))
            }
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Success(AN_EVENT_ID, 0))
            }
        }
    }

    @Test
    fun `present - focus on event error case`() = runTest {
        val presenter = createTimelinePresenter(
            room = FakeMatrixRoom(
                liveTimeline = FakeTimeline(
                    timelineItems = flowOf(emptyList()),
                ),
                timelineFocusedOnEventResult = { Result.failure(Throwable("An error")) },
                canUserSendMessageResult = { _, _ -> Result.success(true) },
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(TimelineEvents.FocusOnEvent(AN_EVENT_ID))
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Requested(AN_EVENT_ID, Duration.ZERO))
            }
            awaitItem().also { state ->
                assertThat(state.focusedEventId).isEqualTo(AN_EVENT_ID)
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))
            }
            awaitItem().also { state ->
                assertThat(state.focusRequestState).isInstanceOf(FocusRequestState.Failure::class.java)
                state.eventSink(TimelineEvents.ClearFocusRequestState)
            }
            awaitItem().also { state ->
                assertThat(state.focusRequestState).isEqualTo(FocusRequestState.None)
            }
        }
    }

    @Test
    fun `present - show shield hide shield`() = runTest {
        val presenter = createTimelinePresenter()
        val shield = aCriticalShield()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.messageShield).isNull()
            initialState.eventSink(TimelineEvents.ShowShieldDialog(shield))
            awaitItem().also { state ->
                assertThat(state.messageShield).isEqualTo(shield)
                state.eventSink(TimelineEvents.HideShieldDialog)
            }
            awaitItem().also { state ->
                assertThat(state.messageShield).isNull()
            }
        }
    }

    @Test
    fun `present - when room member info is loaded, read receipts info should be updated`() = runTest {
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(
                        A_UNIQUE_ID,
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
        )
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
        ).apply {
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
        return awaitItem()
    }
}

internal fun TestScope.createTimelinePresenter(
    timeline: Timeline = FakeTimeline(),
    room: FakeMatrixRoom = FakeMatrixRoom(
        liveTimeline = timeline,
        canUserSendMessageResult = { _, _ -> Result.success(true) }
    ),
    redactedVoiceMessageManager: RedactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
    messagesNavigator: FakeMessagesNavigator = FakeMessagesNavigator(),
    endPollAction: EndPollAction = FakeEndPollAction(),
    sendPollResponseAction: SendPollResponseAction = FakeSendPollResponseAction(),
    sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
    timelineItemIndexer: TimelineItemIndexer = TimelineItemIndexer(),
): TimelinePresenter {
    return TimelinePresenter(
        timelineItemsFactoryCreator = aTimelineItemsFactoryCreator(),
        room = room,
        dispatchers = testCoroutineDispatchers(),
        appScope = this,
        navigator = messagesNavigator,
        redactedVoiceMessageManager = redactedVoiceMessageManager,
        endPollAction = endPollAction,
        sendPollResponseAction = sendPollResponseAction,
        sessionPreferencesStore = sessionPreferencesStore,
        timelineItemIndexer = timelineItemIndexer,
        timelineController = TimelineController(room),
        resolveVerifiedUserSendFailurePresenter = { aResolveVerifiedUserSendFailureState() },
        typingNotificationPresenter = { aTypingNotificationState() },
    )
}
