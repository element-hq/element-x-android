/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceFiltersPresenterTest {
    @Test
    fun `present - when feature flag is disabled returns Disabled state`() = runTest {
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to false)
            )
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state).isEqualTo(SpaceFiltersState.Disabled)
        }
    }

    @Test
    fun `present - when feature flag is enabled returns Unselected state initially`() = runTest {
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            )
        )
        presenter.test {
            val state = awaitLastSequentialItem()
            assertThat(state).isInstanceOf(SpaceFiltersState.Unselected::class.java)
        }
    }

    @Test
    fun `present - ShowFilters event transitions from Unselected to Selecting`() = runTest {
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            )
        )
        presenter.test {
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            val selectingState = awaitLastSequentialItem()
            assertThat(selectingState).isInstanceOf(SpaceFiltersState.Selecting::class.java)
        }
    }

    @Test
    fun `present - Cancel event in Selecting state transitions back to Unselected`() = runTest {
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            )
        )
        presenter.test {
            // Start in Unselected
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            // Now in Selecting
            val selectingState = awaitLastSequentialItem() as SpaceFiltersState.Selecting
            selectingState.eventSink(SpaceFiltersEvent.Selecting.Cancel)

            // Back to Unselected
            val finalState = awaitLastSequentialItem()
            assertThat(finalState).isInstanceOf(SpaceFiltersState.Unselected::class.java)
        }
    }

    @Test
    fun `present - SelectFilter event in Selecting state transitions to Selected`() = runTest {
        val spaceFilter = aSpaceServiceFilter(displayName = "Test Space")
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            )
        )
        presenter.test {
            // Start in Unselected
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            // Now in Selecting
            val selectingState = awaitLastSequentialItem() as SpaceFiltersState.Selecting
            selectingState.eventSink(SpaceFiltersEvent.Selecting.SelectFilter(spaceFilter))

            // Now in Selected
            val selectedState = awaitLastSequentialItem() as SpaceFiltersState.Selected
            assertThat(selectedState.selectedFilter).isEqualTo(spaceFilter)
        }
    }

    @Test
    fun `present - ClearSelection event in Selected state transitions back to Unselected`() = runTest {
        val spaceFilter = aSpaceServiceFilter(displayName = "Test Space")
        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            )
        )
        presenter.test {
            // Start in Unselected
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            // Now in Selecting
            val selectingState = awaitLastSequentialItem() as SpaceFiltersState.Selecting
            selectingState.eventSink(SpaceFiltersEvent.Selecting.SelectFilter(spaceFilter))

            // Now in Selected
            val selectedState = awaitLastSequentialItem() as SpaceFiltersState.Selected
            selectedState.eventSink(SpaceFiltersEvent.Selected.ClearSelection)

            // Back to Unselected
            val finalState = awaitLastSequentialItem()
            assertThat(finalState).isInstanceOf(SpaceFiltersState.Unselected::class.java)
        }
    }

    @Test
    fun `present - available filters are passed from SpaceService`() = runTest {
        val spaceFilter1 = aSpaceServiceFilter(displayName = "Work", roomId = RoomId("!work:example.com"))
        val spaceFilter2 = aSpaceServiceFilter(displayName = "Personal", roomId = RoomId("!personal:example.com"))
        val spaceFilters = listOf(spaceFilter1, spaceFilter2)

        val spaceService = FakeSpaceService()
        val matrixClient = FakeMatrixClient(spaceService = spaceService)

        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            ),
            matrixClient = matrixClient,
        )
        presenter.test {
            // Start in Unselected
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            // Emit space filters
            spaceService.emitSpaceFilters(spaceFilters)

            // Now in Selecting with available filters
            val selectingState = awaitLastSequentialItem() as SpaceFiltersState.Selecting
            assertThat(selectingState.availableFilters).containsExactly(spaceFilter1, spaceFilter2).inOrder()
        }
    }

    @Test
    fun `present - selected filter stays in sync when available filters update`() = runTest {
        val originalFilter = aSpaceServiceFilter(
            displayName = "Work",
            roomId = RoomId("!work:example.com"),
            descendants = listOf(RoomId("!room1:example.com"))
        )
        val updatedFilter = aSpaceServiceFilter(
            displayName = "Work",
            roomId = RoomId("!work:example.com"),
            descendants = listOf(RoomId("!room1:example.com"), RoomId("!room2:example.com"))
        )

        val spaceService = FakeSpaceService()
        val matrixClient = FakeMatrixClient(spaceService = spaceService)

        val presenter = createSpaceFiltersPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.RoomListSpaceFilters.key to true)
            ),
            matrixClient = matrixClient,
        )
        presenter.test {
            // Start in Unselected
            val unselectedState = awaitLastSequentialItem() as SpaceFiltersState.Unselected
            unselectedState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)

            // Emit initial space filters
            spaceService.emitSpaceFilters(listOf(originalFilter))

            // Now in Selecting
            val selectingState = awaitLastSequentialItem() as SpaceFiltersState.Selecting
            selectingState.eventSink(SpaceFiltersEvent.Selecting.SelectFilter(originalFilter))

            // Now in Selected
            val selectedState = awaitLastSequentialItem() as SpaceFiltersState.Selected
            assertThat(selectedState.selectedFilter.descendants).hasSize(1)

            // Emit updated space filters
            spaceService.emitSpaceFilters(listOf(updatedFilter))

            // Selected filter should be updated
            val updatedSelectedState = awaitLastSequentialItem() as SpaceFiltersState.Selected
            assertThat(updatedSelectedState.selectedFilter.descendants).hasSize(2)
        }
    }

    private fun createSpaceFiltersPresenter(
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
    ): SpaceFiltersPresenter {
        return SpaceFiltersPresenter(
            featureFlagService = featureFlagService,
            matrixClient = matrixClient,
        )
    }
}
