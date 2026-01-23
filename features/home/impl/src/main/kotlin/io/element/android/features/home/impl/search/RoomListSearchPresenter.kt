/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.persistentListOf

@Inject
class RoomListSearchPresenter(
    private val dataSourceFactory: RoomListSearchDataSource.Factory,
) : Presenter<RoomListSearchState> {
    @Composable
    override fun present(): RoomListSearchState {
        // Do not use rememberSaveable so that search is not active when the user navigates back to the screen
        var isSearchActive by remember {
            mutableStateOf(false)
        }
        val searchQuery = rememberTextFieldState()

        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { dataSourceFactory.create(coroutineScope) }

        LaunchedEffect(isSearchActive) {
            dataSource.setIsActive(isSearchActive)
        }

        LaunchedEffect(searchQuery.text) {
            dataSource.setSearchQuery(searchQuery.text.toString())
        }

        fun handleEvent(event: RoomListSearchEvents) {
            when (event) {
                RoomListSearchEvents.ClearQuery -> {
                    searchQuery.clearText()
                }
                RoomListSearchEvents.ToggleSearchVisibility -> {
                    isSearchActive = !isSearchActive
                    searchQuery.clearText()
                }
            }
        }

        val searchResults by dataSource.roomSummaries.collectAsState(initial = persistentListOf())

        return RoomListSearchState(
            isSearchActive = isSearchActive,
            query = searchQuery,
            results = searchResults,
            eventSink = ::handleEvent,
        )
    }
}
