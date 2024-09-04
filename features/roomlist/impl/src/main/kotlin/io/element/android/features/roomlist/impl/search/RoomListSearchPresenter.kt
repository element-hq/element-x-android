/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

class RoomListSearchPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    private val dataSource: RoomListSearchDataSource,
    private val featureFlagService: FeatureFlagService,
) : Presenter<RoomListSearchState> {
    @Composable
    override fun present(): RoomListSearchState {
        // Do not use rememberSaveable so that search is not active when the user navigates back to the screen
        var isSearchActive by remember {
            mutableStateOf(false)
        }
        var searchQuery by remember {
            mutableStateOf("")
        }

        LaunchedEffect(isSearchActive) {
            dataSource.setIsActive(isSearchActive)
        }

        LaunchedEffect(searchQuery) {
            dataSource.setSearchQuery(searchQuery)
        }

        fun handleEvents(event: RoomListSearchEvents) {
            when (event) {
                RoomListSearchEvents.ClearQuery -> {
                    searchQuery = ""
                }
                is RoomListSearchEvents.QueryChanged -> {
                    searchQuery = event.query
                }
                RoomListSearchEvents.ToggleSearchVisibility -> {
                    isSearchActive = !isSearchActive
                    searchQuery = ""
                }
            }
        }

        val isRoomDirectorySearchEnabled by featureFlagService.isFeatureEnabledFlow(FeatureFlags.RoomDirectorySearch).collectAsState(initial = false)
        val searchResults by dataSource.roomSummaries.collectAsState(initial = persistentListOf())

        return RoomListSearchState(
            isDebugBuild = buildMeta.isDebuggable,
            isSearchActive = isSearchActive,
            query = searchQuery,
            results = searchResults,
            isRoomDirectorySearchEnabled = isRoomDirectorySearchEnabled,
            eventSink = ::handleEvents
        )
    }
}
