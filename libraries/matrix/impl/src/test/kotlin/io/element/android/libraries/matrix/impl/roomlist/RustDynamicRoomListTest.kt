/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListDynamicEntriesController
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustDynamicRoomListTest {
    @Test
    fun `updateFilter before the controller is created applies the filter when it is created`() = runTest {
        val sut = createRustDynamicRoomList()
        sut.updateFilter(RoomListFilter.NormalizedMatchRoomName("query"))
        val controller = FakeFfiRoomListDynamicEntriesController()
        sut.onControllerCreated(controller, initialFilter = RoomListFilter.all())
        assertThat(controller.filters).containsExactly(RoomListFilterMapper.toRustFilter(RoomListFilter.NormalizedMatchRoomName("query")))
    }

    @Test
    fun `when the filter is not updated, the controller filter is not changed when it is created`() = runTest {
        val sut = createRustDynamicRoomList()
        val controller = FakeFfiRoomListDynamicEntriesController()
        sut.onControllerCreated(controller, initialFilter = RoomListFilter.all())
        assertThat(controller.filters).isEmpty()
    }

    @Test
    fun `updateFilter after the controller is created applies the filter`() = runTest {
        val sut = createRustDynamicRoomList()
        val controller = FakeFfiRoomListDynamicEntriesController()
        sut.onControllerCreated(controller, initialFilter = RoomListFilter.all())
        sut.updateFilter(RoomListFilter.NormalizedMatchRoomName("query"))
        assertThat(controller.filters).containsExactly(RoomListFilterMapper.toRustFilter(RoomListFilter.NormalizedMatchRoomName("query")))
    }

    private fun TestScope.createRustDynamicRoomList(): RustDynamicRoomList {
        val summaries = MutableSharedFlow<List<RoomSummary>>(replay = 1)
        return RustDynamicRoomList(
            summaries = summaries,
            loadingState = MutableStateFlow(RoomList.LoadingState.NotLoaded),
            processor = RoomSummaryListProcessor(
                summaries,
                FakeFfiRoomListService(),
                coroutineContext = StandardTestDispatcher(testScheduler),
                roomSummaryFactory = RoomSummaryFactory(),
                analyticsService = FakeAnalyticsService(),
            ),
            pageSize = 10,
            initialFilter = RoomListFilter.all(),
        )
    }
}
