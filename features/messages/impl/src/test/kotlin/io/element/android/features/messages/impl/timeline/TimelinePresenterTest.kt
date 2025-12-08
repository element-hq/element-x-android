/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.features.roomcall.api.aStandByCallState
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.tombstone.PredecessorRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.event.Receipt
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID_2
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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

    @Test
    fun `present - on scroll finished mark a room as read if the first visible index is 0 - read private`() {
        `present - on scroll finished mark a room as read if the first visible index is 0`(
            isSendPublicReadReceiptsEnabled = false,
            expectedReceiptType = ReceiptType.READ_PRIVATE,
        )
    }

    @Test
    fun `present - on scroll finished mark a room as read if the first visible index is 0 - read`() {
        `present - on scroll finished mark a room as read if the first visible index is 0`(
            isSendPublicReadReceiptsEnabled = true,
            expectedReceiptType = ReceiptType.READ,
        )
    }

    private fun `present - on scroll finished mark a room as read if the first visible index is 0`(
        isSendPublicReadReceiptsEnabled: Boolean,
        expectedReceiptType: ReceiptType,
    ) = runTest(StandardTestDispatcher()) {
        val markAsReadResult = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val sendReadReceiptLambda = lambdaRecorder<EventId, ReceiptType, Result<Unit>> { _, _ -> Result.success(Unit) }
        val timeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
                )
            ),
            markAsReadResult = markAsReadResult,
            sendReadReceiptLambda = sendReadReceiptLambda,
        )
        val room = FakeJoinedRoom(
            liveTimeline = timeline,
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
            )
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = isSendPublicReadReceiptsEnabled)
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
            assert(markAsReadResult)
                .isCalledOnce()
                .with(value(expectedReceiptType))
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
            ),
            markAsReadResult = { Result.success(Unit) },
            sendReadReceiptLambda = sendReadReceiptsLambda,
        )
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
        val timeline = FakeTimeline(
            timelineItems = timelineItems,
            markAsReadResult = { Result.success(Unit) },
        )
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
        val onEditPollClickLambda = lambdaRecorder { _: EventId -> }
        val navigator = FakeMessagesNavigator(
            onEditPollClickLambda = onEditPollClickLambda
        )
        val presenter = createTimelinePresenter(
            messagesNavigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().eventSink(TimelineEvents.EditPoll(AN_EVENT_ID))
            onEditPollClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
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
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) },
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                threadRootIdForEventResult = { _ -> Result.success(null) },
            ),
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
        val timelineItemIndexer = TimelineItemIndexer()
        val presenter = createTimelinePresenter(
            room = FakeJoinedRoom(
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
                baseRoom = FakeBaseRoom(
                    canUserSendMessageResult = { _, _ -> Result.success(true) },
                    threadRootIdForEventResult = { Result.success(null) },
                ),
            ),
            timelineItemIndexer = timelineItemIndexer,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()

            advanceUntilIdle()

            // Pre-populate the indexer after the first items have been retrieved
            timelineItemIndexer.process(listOf(aMessageEvent(eventId = AN_EVENT_ID)))

            initialState.eventSink.invoke(TimelineEvents.FocusOnEvent(AN_EVENT_ID))

            advanceUntilIdle()

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
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline(
                    timelineItems = flowOf(emptyList()),
                ),
                createTimelineResult = { Result.failure(RuntimeException("An error")) },
                baseRoom = FakeBaseRoom(
                    canUserSendMessageResult = { _, _ -> Result.success(true) },
                    threadRootIdForEventResult = { _ -> Result.success(null) },
                ),
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
    fun `present - focus on event in a thread opens the thread`() = runTest {
        val threadId = A_THREAD_ID
        val detachedTimeline = FakeTimeline(
            mode = Timeline.Mode.FocusedOnEvent(AN_EVENT_ID_2),
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
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) },
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                threadRootIdForEventResult = { _ -> Result.success(threadId) },
            ),
        )
        val openThreadLambda = lambdaRecorder { _: ThreadId, _: EventId? -> }
        val navigator = FakeMessagesNavigator(onOpenThreadLambda = openThreadLambda)
        val presenter = createTimelinePresenter(
            room = room,
            timeline = liveTimeline,
            messagesNavigator = navigator,
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

            advanceUntilIdle()

            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))

            // The live timeline focuses in the thread root
            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Success(A_THREAD_ID.asEventId()))

            // The thread is opened
            openThreadLambda.assertions()
                .isCalledOnce()
                .with(
                    value(threadId),
                    value(AN_EVENT_ID),
                )
        }
    }

    @Test
    fun `present - focus on event in a thread when in the same thread just moves the focus`() = runTest {
        val threadId = A_THREAD_ID
        val detachedTimeline = FakeTimeline(
            mode = Timeline.Mode.FocusedOnEvent(AN_EVENT_ID_2),
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
            mode = Timeline.Mode.Thread(threadId),
            timelineItems = flowOf(emptyList())
        )
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) },
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                threadRootIdForEventResult = { _ -> Result.success(threadId) },
            ),
        )
        val openThreadLambda = lambdaRecorder { _: ThreadId, _: EventId? -> }
        val navigator = FakeMessagesNavigator(onOpenThreadLambda = openThreadLambda)
        val presenter = createTimelinePresenter(
            room = room,
            timeline = liveTimeline,
            messagesNavigator = navigator,
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

            advanceUntilIdle()

            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))

            // The live timeline focuses in the event directly since we are already in the thread
            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Success(AN_EVENT_ID))

            // The thread is not opened again
            openThreadLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - focus on event in a thread when in a different thread opens the new thread`() = runTest {
        val currentThreadId = A_THREAD_ID
        val detachedTimeline = FakeTimeline(
            mode = Timeline.Mode.FocusedOnEvent(AN_EVENT_ID_2),
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
            mode = Timeline.Mode.Thread(currentThreadId),
            timelineItems = flowOf(emptyList())
        )
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) },
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                // Use a different thread id
                threadRootIdForEventResult = { _ -> Result.success(A_THREAD_ID_2) },
            ),
        )
        val openThreadLambda = lambdaRecorder { _: ThreadId, _: EventId? -> }
        val navigator = FakeMessagesNavigator(onOpenThreadLambda = openThreadLambda)
        val presenter = createTimelinePresenter(
            room = room,
            timeline = liveTimeline,
            messagesNavigator = navigator,
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

            advanceUntilIdle()

            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))

            // The live timeline focuses in the event directly since we are already in the thread
            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Success(A_THREAD_ID_2.asEventId()))

            // The other thread is opened
            openThreadLambda.assertions()
                .isCalledOnce()
                .with(
                    value(A_THREAD_ID_2),
                    value(AN_EVENT_ID),
                )
        }
    }

    @Test
    fun `present - focus on event in a the room while in a thread of that room opens the room`() = runTest {
        val detachedTimeline = FakeTimeline(
            mode = Timeline.Mode.FocusedOnEvent(AN_EVENT_ID_2),
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
            mode = Timeline.Mode.Thread(A_THREAD_ID),
            timelineItems = flowOf(emptyList())
        )
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(detachedTimeline) },
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                // The event is in the main timeline, not in a thread
                threadRootIdForEventResult = { _ -> Result.success(null) },
            ),
        )
        val openRoomLambda = lambdaRecorder { _: RoomId, _: EventId?, _: List<String> -> }
        val navigator = FakeMessagesNavigator(onNavigateToRoomLambda = openRoomLambda)
        val presenter = createTimelinePresenter(
            room = room,
            timeline = liveTimeline,
            messagesNavigator = navigator,
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

            advanceUntilIdle()

            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.Loading(AN_EVENT_ID))

            // The focus state will reset
            assertThat(awaitItem().focusRequestState).isEqualTo(FocusRequestState.None)

            // The room is opened again
            openRoomLambda.assertions()
                .isCalledOnce()
                .with(
                    value(room.roomId),
                    value(AN_EVENT_ID),
                    value(emptyList<String>())
                )
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
        val room = FakeJoinedRoom(
            liveTimeline = timeline,
            baseRoom = FakeBaseRoom(canUserSendMessageResult = { _, _ -> Result.success(true) }),
        ).apply {
            givenRoomMembersState(RoomMembersState.Unknown)
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
                RoomMembersState.Ready(
                    persistentListOf(aRoomMember(userId = A_USER_ID, avatarUrl = avatarUrl))
                )
            )

            val updatedEvent = awaitItem().timelineItems.first() as TimelineItem.Event
            assertThat(updatedEvent.readReceiptState.receipts.first().avatarData.url).isEqualTo(avatarUrl)
        }
    }

    @Test
    fun `present - timeline room info includes predecessor room when room has predecessor`() = runTest {
        val predecessorRoomId = RoomId("!predecessor:server.org")
        val predecessorRoom = PredecessorRoom(roomId = predecessorRoomId)

        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                predecessorRoomResult = { predecessorRoom }
            ),
        )

        val presenter = createTimelinePresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.timelineRoomInfo.predecessorRoom).isNotNull()
            assertThat(initialState.timelineRoomInfo.predecessorRoom?.roomId).isEqualTo(predecessorRoomId)
        }
    }

    @Test
    fun `present - timeline room info no predecessor`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                predecessorRoomResult = { null }
            ),
        )
        val presenter = createTimelinePresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.timelineRoomInfo.predecessorRoom).isNull()
        }
    }

    @Test
    fun `present - timeline event navigate to room`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
            ),
        )
        val onNavigateToRoomLambda = lambdaRecorder<RoomId, EventId?, List<String>, Unit> { _, _, _ -> }
        val navigator = FakeMessagesNavigator(
            onNavigateToRoomLambda = onNavigateToRoomLambda
        )
        val presenter = createTimelinePresenter(room = room, messagesNavigator = navigator)
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(TimelineEvents.NavigateToPredecessorOrSuccessorRoom(A_ROOM_ID))
            assert(onNavigateToRoomLambda)
                .isCalledOnce()
                .with(
                    value(A_ROOM_ID),
                    // No event id when navigating to a successor/predecessor room
                    value(null),
                    value(emptyList<String>())
                )
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        return awaitItem()
    }

    private fun TestScope.createTimelinePresenter(
        timeline: Timeline = FakeTimeline(),
        room: FakeJoinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            baseRoom = FakeBaseRoom(canUserSendMessageResult = { _, _ -> Result.success(true) }),
        ),
        redactedVoiceMessageManager: RedactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
        messagesNavigator: FakeMessagesNavigator = FakeMessagesNavigator(),
        endPollAction: EndPollAction = FakeEndPollAction(),
        sendPollResponseAction: SendPollResponseAction = FakeSendPollResponseAction(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        timelineItemIndexer: TimelineItemIndexer = TimelineItemIndexer(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
    ): TimelinePresenter {
        return TimelinePresenter(
            timelineItemsFactoryCreator = aTimelineItemsFactoryCreator(),
            room = room,
            dispatchers = testCoroutineDispatchers(),
            sessionCoroutineScope = this,
            navigator = messagesNavigator,
            redactedVoiceMessageManager = redactedVoiceMessageManager,
            endPollAction = endPollAction,
            sendPollResponseAction = sendPollResponseAction,
            sessionPreferencesStore = sessionPreferencesStore,
            timelineItemIndexer = timelineItemIndexer,
            timelineController = TimelineController(room, timeline),
            resolveVerifiedUserSendFailurePresenter = { aResolveVerifiedUserSendFailureState() },
            typingNotificationPresenter = { aTypingNotificationState() },
            roomCallStatePresenter = { aStandByCallState() },
            featureFlagService = featureFlagService,
            analyticsService = FakeAnalyticsService(),
        )
    }
}
