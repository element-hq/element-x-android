/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.list

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.actionlist.FakeActionListPresenter
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.pinned.PinnedEventsTimelineProvider
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinnedMessagesListPresenterTest {

    @Test
    fun `present - initial state feature disabled`() = runTest {
        val room = FakeMatrixRoom(
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createPinnedMessagesListPresenter(room = room, isFeatureEnabled = false)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(PinnedMessagesListState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state feature enabled`() = runTest {
        val room = FakeMatrixRoom(
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createPinnedMessagesListPresenter(room = room, isFeatureEnabled = true)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(PinnedMessagesListState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - timeline failure state`() = runTest {
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.failure(RuntimeException()) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createPinnedMessagesListPresenter(room = room, isFeatureEnabled = true)
        presenter.test {
            skipItems(3)
            val failureState = awaitItem()
            assertThat(failureState).isEqualTo(PinnedMessagesListState.Failed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - empty state`() = runTest {
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.success(FakeTimeline()) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf()))
        }
        val presenter = createPinnedMessagesListPresenter(room = room, isFeatureEnabled = true)
        presenter.test {
            skipItems(3)
            val emptyState = awaitItem()
            assertThat(emptyState).isEqualTo(PinnedMessagesListState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - filled state`() = runTest {
        val messageContent = aMessageContent("A message")
        val pinnedEventsTimeline = FakeTimeline(
            timelineItems = flowOf(
                listOf(
                    MatrixTimelineItem.Event(
                        uniqueId = "FAKE_UNIQUE_ID",
                        event = anEventTimelineItem(
                            eventId = AN_EVENT_ID,
                            content = messageContent,
                        ),
                    )
                )
            )
        )
        val room = FakeMatrixRoom(
            pinnedEventsTimelineResult = { Result.success(pinnedEventsTimeline) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createPinnedMessagesListPresenter(room = room, isFeatureEnabled = true)
        presenter.test {
            skipItems(3)
            val filledState = awaitItem() as PinnedMessagesListState.Filled
            assertThat(filledState.timelineItems).hasSize(1)
            assertThat(filledState.loadedPinnedMessagesCount).isEqualTo(1)
            assertThat(filledState.userEventPermissions.canRedactOwn).isTrue()
            assertThat(filledState.userEventPermissions.canRedactOther).isTrue()
            assertThat(filledState.userEventPermissions.canPinUnpin).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createPinnedMessagesListPresenter(
        navigator: PinnedMessagesListNavigator = FakePinnedMessagesListNavigator(),
        room: MatrixRoom = FakeMatrixRoom(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        isFeatureEnabled: Boolean = true,
    ): PinnedMessagesListPresenter {
        val timelineProvider = PinnedEventsTimelineProvider(
            room = room,
            networkMonitor = networkMonitor,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.PinnedEvents.key to isFeatureEnabled)
            )
        )
        timelineProvider.launchIn(backgroundScope)
        return PinnedMessagesListPresenter(
            navigator = navigator,
            room = room,
            timelineItemsFactory = aTimelineItemsFactory(),
            timelineProvider = timelineProvider,
            snackbarDispatcher = SnackbarDispatcher(),
            actionListPresenterFactory = FakeActionListPresenter.Factory,
        )
    }
}
