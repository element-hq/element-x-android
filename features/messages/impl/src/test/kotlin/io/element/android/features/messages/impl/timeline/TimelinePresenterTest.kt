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
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.PollEnd
import im.vector.app.features.analytics.plan.PollVote
import io.element.android.features.messages.impl.FakeMessagesNavigator
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.features.messages.impl.voicemessages.timeline.FakeRedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.messages.impl.voicemessages.timeline.aRedactedMatrixTimeline
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aMessageContent
import io.element.android.libraries.matrix.test.room.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.awaitWithLatch
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.element.android.tests.testutils.waitForPredicate
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Date

class TimelinePresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.timelineItems).isEmpty()
            val loadedNoTimelineState = awaitItem()
            assertThat(loadedNoTimelineState.timelineItems).isEmpty()
            assertThat(loadedNoTimelineState.sessionState).isEqualTo(SessionState(isSessionVerified = false, isKeyBackupEnabled = false))
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.paginationState.hasMoreToLoadBackwards).isTrue()
            assertThat(initialState.paginationState.isBackPaginating).isFalse()
            initialState.eventSink.invoke(TimelineEvents.LoadMore)
            val inPaginationState = awaitItem()
            assertThat(inPaginationState.paginationState.isBackPaginating).isTrue()
            assertThat(inPaginationState.paginationState.hasMoreToLoadBackwards).isTrue()
            val postPaginationState = awaitItem()
            assertThat(postPaginationState.paginationState.hasMoreToLoadBackwards).isTrue()
            assertThat(postPaginationState.paginationState.isBackPaginating).isFalse()
        }
    }

    @Test
    fun `present - set highlighted event`() = runTest {
        val presenter = createTimelinePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
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

    @Test
    fun `present - on scroll finished send read receipt if an event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(0, anEventTimelineItem())
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt if no event is before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Event(0, anEventTimelineItem())
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(1))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - on scroll finished will not send read receipt only virtual events exist before the index`() = runTest {
        val timeline = FakeMatrixTimeline(
            initialTimelineItems = listOf(
                MatrixTimelineItem.Virtual(0, VirtualTimelineItem.ReadMarker)
            )
        )
        val presenter = createTimelinePresenter(timeline)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
            val initialState = awaitItem()
            // Wait for timeline items to be populated
            skipItems(1)
            awaitWithLatch { latch ->
                timeline.sendReadReceiptLatch = latch
                initialState.eventSink.invoke(TimelineEvents.OnScrollFinished(0))
            }
            assertThat(timeline.sendReadReceiptCount).isEqualTo(0)
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
            val initialState = awaitItem()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            timeline.updateTimelineItems {
                listOf(MatrixTimelineItem.Event(0, anEventTimelineItem(content = aMessageContent())))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 1 }
            // Mimics sending a message, and assert newEventState is FromMe
            timeline.updateTimelineItems { items ->
                val event = anEventTimelineItem(content = aMessageContent(), localSendState = LocalEventSendState.Sent(AN_EVENT_ID))
                items + listOf(MatrixTimelineItem.Event(1, event))
            }
            consumeItemsUntilPredicate { it.timelineItems.size == 2 }
            awaitLastSequentialItem().also { state ->
                assertThat(state.newEventState).isEqualTo(NewEventState.FromMe)
            }
            // Mimics receiving a message without clearing the previous FromMe
            timeline.updateTimelineItems { items ->
                val event = anEventTimelineItem(content = aMessageContent())
                items + listOf(MatrixTimelineItem.Event(2, event))
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
                items + listOf(MatrixTimelineItem.Event(3, event))
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
            val initialState = awaitItem()
            assertThat(initialState.newEventState).isEqualTo(NewEventState.None)
            assertThat(initialState.timelineItems.size).isEqualTo(0)
            val now = Date().time
            val minuteInMillis = 60 * 1000
            // Use index as a convenient value for timestamp
            val (alice, bob, charlie) = aMatrixUserList().take(3).mapIndexed { i, user ->
                ReactionSender(senderId = user.userId, timestamp = now + i * minuteInMillis)
            }
            val oneReaction = listOf(
                EventReaction(
                    key = "❤️",
                    senders = listOf(alice, charlie)
                ),
                EventReaction(
                    key = "👍",
                    senders = listOf(alice, bob)
                ),
                EventReaction(
                    key = "🐶",
                    senders = listOf(charlie)
                ),
            )
            timeline.updateTimelineItems {
                listOf(MatrixTimelineItem.Event(0, anEventTimelineItem(reactions = oneReaction)))
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
    fun `present - PollAnswerSelected event calls into rust room api and analytics`() = runTest {
        val room = FakeMatrixRoom()
        val analyticsService = FakeAnalyticsService()
        val presenter = createTimelinePresenter(
            room = room,
            analyticsService = analyticsService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(TimelineEvents.PollAnswerSelected(AN_EVENT_ID, "anAnswerId"))
        }
        delay(1)
        assertThat(room.sendPollResponseInvocations.size).isEqualTo(1)
        assertThat(room.sendPollResponseInvocations.first().answers).isEqualTo(listOf("anAnswerId"))
        assertThat(room.sendPollResponseInvocations.first().pollStartId).isEqualTo(AN_EVENT_ID)
        assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
        assertThat(analyticsService.capturedEvents.last()).isEqualTo(PollVote())
    }

    @Test
    fun `present - PollEndClicked event calls into rust room api and analytics`() = runTest {
        val room = FakeMatrixRoom()
        val analyticsService = FakeAnalyticsService()
        val presenter = createTimelinePresenter(
            room = room,
            analyticsService = analyticsService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(TimelineEvents.PollEndClicked(aMessageEvent().eventId!!))
            waitForPredicate { room.endPollInvocations.size == 1 }
            cancelAndIgnoreRemainingEvents()
            assertThat(room.endPollInvocations.size).isEqualTo(1)
            assertThat(room.endPollInvocations.first().pollStartId).isEqualTo(AN_EVENT_ID)
            assertThat(room.endPollInvocations.first().text).isEqualTo("The poll with event id: \$anEventId has ended.")
            assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(analyticsService.capturedEvents.last()).isEqualTo(PollEnd())
        }
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
            awaitItem().eventSink(TimelineEvents.PollEditClicked(AN_EVENT_ID))
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
            skipItems(1) // skip initial state
            assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(0)
            awaitItem().let {
                assertThat(it.timelineItems).isNotEmpty()
                assertThat(redactedVoiceMessageManager.invocations.size).isEqualTo(1)
            }
        }
    }

    private fun TestScope.createTimelinePresenter(
        timeline: MatrixTimeline = FakeMatrixTimeline(),
        timelineItemsFactory: TimelineItemsFactory = aTimelineItemsFactory(),
        redactedVoiceMessageManager: RedactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
        messagesNavigator: FakeMessagesNavigator = FakeMessagesNavigator(),
    ): TimelinePresenter {
        return TimelinePresenter(
            timelineItemsFactory = timelineItemsFactory,
            room = FakeMatrixRoom(matrixTimeline = timeline),
            dispatchers = testCoroutineDispatchers(),
            appScope = this,
            navigator = messagesNavigator,
            analyticsService = FakeAnalyticsService(),
            encryptionService = FakeEncryptionService(),
            verificationService = FakeSessionVerificationService(),
            featureFlagService = FakeFeatureFlagService(),
            redactedVoiceMessageManager = redactedVoiceMessageManager,
        )
    }

    private fun TestScope.createTimelinePresenter(
        room: MatrixRoom,
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ): TimelinePresenter {
        return TimelinePresenter(
            timelineItemsFactory = aTimelineItemsFactory(),
            room = room,
            dispatchers = testCoroutineDispatchers(),
            appScope = this,
            navigator = FakeMessagesNavigator(),
            analyticsService = analyticsService,
            encryptionService = FakeEncryptionService(),
            verificationService = FakeSessionVerificationService(),
            featureFlagService = FakeFeatureFlagService(),
            redactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
        )
    }
}
