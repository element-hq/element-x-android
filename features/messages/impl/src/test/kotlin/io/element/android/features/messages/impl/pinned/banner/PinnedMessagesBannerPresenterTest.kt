/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.banner

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.pinned.PinnedEventsTimelineProvider
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.eventformatter.test.FakePinnedMessagesBannerFormatter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID_2
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinnedMessagesBannerPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPinnedMessagesBannerPresenter(isFeatureEnabled = true)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(PinnedMessagesBannerState.Hidden)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - feature disabled`() = runTest {
        val presenter = createPinnedMessagesBannerPresenter(isFeatureEnabled = false)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(PinnedMessagesBannerState.Hidden)
        }
    }

    @Test
    fun `present - loading state`() = runTest {
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.success(FakeTimeline()) }
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createPinnedMessagesBannerPresenter(room = room)
        presenter.test {
            skipItems(2)
            val loadingState = awaitItem()
            assertThat(loadingState).isEqualTo(PinnedMessagesBannerState.Loading(1))
            assertThat(loadingState.pinnedMessagesCount()).isEqualTo(1)
            assertThat(loadingState.currentPinnedMessageIndex()).isEqualTo(0)
        }
    }

    @Test
    fun `present - loaded state`() = runTest {
        val messageContent = aMessageContent("A message")
        val pinnedEventsTimeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID,
                            content = messageContent,
                        ),
                    )
                )
            )
        )
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.success(pinnedEventsTimeline) }
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID, AN_EVENT_ID_2)))
        }
        val presenter = createPinnedMessagesBannerPresenter(room = room)
        presenter.test {
            skipItems(3)
            val loadedState = awaitItem() as PinnedMessagesBannerState.Loaded
            assertThat(loadedState.currentPinnedMessageIndex).isEqualTo(0)
            assertThat(loadedState.loadedPinnedMessagesCount).isEqualTo(1)
            assertThat(loadedState.currentPinnedMessage.formatted.text).isEqualTo(messageContent.toString())
        }
    }

    @Test
    fun `present - loaded state - multiple pinned messages`() = runTest {
        val messageContent1 = aMessageContent("A message")
        val messageContent2 = aMessageContent("Another message")
        val pinnedEventsTimeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID,
                            content = messageContent1,
                        ),
                    ),
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID_2,
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID_2,
                            content = messageContent2,
                        ),
                    )
                )
            )
        )
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.success(pinnedEventsTimeline) }
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID, AN_EVENT_ID_2)))
        }
        val presenter = createPinnedMessagesBannerPresenter(room = room)
        presenter.test {
            skipItems(3)
            awaitItem().also { loadedState ->
                loadedState as PinnedMessagesBannerState.Loaded
                assertThat(loadedState.currentPinnedMessageIndex).isEqualTo(1)
                assertThat(loadedState.loadedPinnedMessagesCount).isEqualTo(2)
                assertThat(loadedState.currentPinnedMessage.formatted.text).isEqualTo(messageContent2.toString())
                loadedState.eventSink(PinnedMessagesBannerEvents.MoveToNextPinned)
            }

            awaitItem().also { loadedState ->
                loadedState as PinnedMessagesBannerState.Loaded
                assertThat(loadedState.currentPinnedMessageIndex).isEqualTo(0)
                assertThat(loadedState.loadedPinnedMessagesCount).isEqualTo(2)
                assertThat(loadedState.currentPinnedMessage.formatted.text).isEqualTo(messageContent1.toString())
                loadedState.eventSink(PinnedMessagesBannerEvents.MoveToNextPinned)
            }

            awaitItem().also { loadedState ->
                loadedState as PinnedMessagesBannerState.Loaded
                assertThat(loadedState.currentPinnedMessageIndex).isEqualTo(1)
                assertThat(loadedState.loadedPinnedMessagesCount).isEqualTo(2)
                assertThat(loadedState.currentPinnedMessage.formatted.text).isEqualTo(messageContent2.toString())
            }
        }
    }

    @Test
    fun `present - timeline failed`() = runTest {
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.failure(Exception()) }
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createPinnedMessagesBannerPresenter(room = room)
        presenter.test {
            skipItems(2)
            awaitItem().also { loadingState ->
                assertThat(loadingState).isEqualTo(PinnedMessagesBannerState.Loading(1))
                assertThat(loadingState.pinnedMessagesCount()).isEqualTo(1)
                assertThat(loadingState.currentPinnedMessageIndex()).isEqualTo(0)
            }
            awaitItem().also { failedState ->
                assertThat(failedState).isEqualTo(PinnedMessagesBannerState.Hidden)
            }
        }
    }

    private fun TestScope.createPinnedMessagesBannerPresenter(
        room: MatrixRoom = FakeMatrixRoom(),
        itemFactory: PinnedMessagesBannerItemFactory = PinnedMessagesBannerItemFactory(
            coroutineDispatchers = testCoroutineDispatchers(),
            formatter = FakePinnedMessagesBannerFormatter(
                formatLambda = { event -> "${event.content}" }
            )
        ),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        isFeatureEnabled: Boolean = true,
    ): PinnedMessagesBannerPresenter {
        val timelineProvider = PinnedEventsTimelineProvider(
            room = room,
            networkMonitor = networkMonitor,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.PinnedEvents.key to isFeatureEnabled)
            )
        )
        timelineProvider.launchIn(backgroundScope)

        return PinnedMessagesBannerPresenter(
            room = room,
            itemFactory = itemFactory,
            pinnedEventsTimelineProvider = timelineProvider,
        )
    }
}
