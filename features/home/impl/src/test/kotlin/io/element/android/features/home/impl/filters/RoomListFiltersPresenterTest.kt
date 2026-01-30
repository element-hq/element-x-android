/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.FakeDateTimeObserver
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.datasource.aRoomListRoomSummaryFactory
import io.element.android.features.home.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListFiltersPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                    filterSelectionState(RoomListFilter.Invites, false),
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present - toggle rooms filter`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListFiltersPresenter(roomListService)
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Rooms, true),
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                ).inOrder()

                assertThat(state.selectedFilters()).containsExactly(
                    RoomListFilter.Rooms,
                )
                state.eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            }
            advanceUntilIdle()
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                    filterSelectionState(RoomListFilter.Invites, false),
                ).inOrder()
                assertThat(state.selectedFilters()).isEmpty()
            }
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present - clear filters event`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListFiltersPresenter(roomListService)
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                state.eventSink.invoke(RoomListFiltersEvent.ClearSelectedFilters)
            }
            advanceUntilIdle()
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
            }
        }
    }
}

private fun filterSelectionState(filter: RoomListFilter, selected: Boolean) = FilterSelectionState(
    filter = filter,
    isSelected = selected,
)

private fun TestScope.createRoomListFiltersPresenter(
    roomListService: RoomListService = FakeRoomListService(),
    notificationSettingsService: NotificationSettingsService = FakeNotificationSettingsService(),
    dateFormatter: DateFormatter = FakeDateFormatter(),
    roomLatestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
): RoomListFiltersPresenter {
    return RoomListFiltersPresenter(
        roomListDataSource = RoomListDataSource(
            roomListService = roomListService,
            roomListRoomSummaryFactory = aRoomListRoomSummaryFactory(
                dateFormatter = dateFormatter,
                roomLatestEventFormatter = roomLatestEventFormatter,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
            notificationSettingsService = notificationSettingsService,
            sessionCoroutineScope = backgroundScope,
            dateTimeObserver = FakeDateTimeObserver(),
            analyticsService = FakeAnalyticsService(),
        ),
        filterSelectionStrategy = DefaultFilterSelectionStrategy(),
    )
}
