/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map

@Inject
class SpaceFiltersPresenter(
    private val featureFlagService: FeatureFlagService,
    private val matrixClient: MatrixClient,
) : Presenter<SpaceFiltersState> {
    @Composable
    override fun present(): SpaceFiltersState {
        val isFeatureEnabled by featureFlagService
            .isFeatureEnabledFlow(FeatureFlags.RoomListSpaceFilters)
            .collectAsState(initial = false)

        val availableFilters by remember {
            matrixClient.spaceService.spaceFiltersFlow.map { it.toImmutableList() }
        }.collectAsState(initial = persistentListOf())

        if (!isFeatureEnabled || availableFilters.isEmpty()) {
            return SpaceFiltersState.Disabled
        }

        var selectionMode by remember { mutableStateOf<SelectionMode>(SelectionMode.Unselected) }

        fun handleUnselectedEvent(event: SpaceFiltersEvent.Unselected) {
            when (event) {
                SpaceFiltersEvent.Unselected.ShowFilters -> {
                    selectionMode = SelectionMode.Selecting
                }
            }
        }

        fun handleSelectingEvent(event: SpaceFiltersEvent.Selecting) {
            when (event) {
                SpaceFiltersEvent.Selecting.Cancel -> {
                    selectionMode = SelectionMode.Unselected
                }
                is SpaceFiltersEvent.Selecting.SelectFilter -> {
                    selectionMode = SelectionMode.Selected(event.spaceFilter)
                }
            }
        }

        fun handleSelectedEvent(event: SpaceFiltersEvent.Selected) {
            when (event) {
                SpaceFiltersEvent.Selected.ClearSelection -> {
                    selectionMode = SelectionMode.Unselected
                }
            }
        }

        return when (val mode = selectionMode) {
            SelectionMode.Unselected -> SpaceFiltersState.Unselected(
                eventSink = ::handleUnselectedEvent,
            )
            SelectionMode.Selecting -> {
                val searchQuery = rememberTextFieldState()
                SpaceFiltersState.Selecting(
                    availableFilters = availableFilters,
                    searchQuery = searchQuery,
                    eventSink = ::handleSelectingEvent,
                )
            }
            is SelectionMode.Selected -> {
                var selectedFilter by remember { mutableStateOf(mode.filter) }
                // Makes sure the selectedFilter stays in sync with the available filters
                LaunchedEffect(availableFilters) {
                    val upToDateFilter = availableFilters
                        .firstOrNull { it.spaceRoom.roomId == mode.filter.spaceRoom.roomId }
                    if (upToDateFilter == null) {
                        selectionMode = SelectionMode.Unselected
                    } else {
                        selectedFilter = upToDateFilter
                    }
                }
                SpaceFiltersState.Selected(
                    selectedFilter = selectedFilter,
                    eventSink = ::handleSelectedEvent,
                )
            }
        }
    }
}

private sealed interface SelectionMode {
    data object Unselected : SelectionMode
    data object Selecting : SelectionMode
    data class Selected(val filter: SpaceServiceFilter) : SelectionMode
}
