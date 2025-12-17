/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.roomdirectory.impl.root.model.RoomDirectoryListState
import io.element.android.features.roomdirectory.impl.root.model.toFeatureModel
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private const val SEARCH_BATCH_SIZE = 20

@Inject
class RoomDirectoryPresenter(
    private val dispatchers: CoroutineDispatchers,
    private val roomDirectoryService: RoomDirectoryService,
) : Presenter<RoomDirectoryState> {
    @Composable
    override fun present(): RoomDirectoryState {
        var loadingMore by remember {
            mutableStateOf(false)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf<String?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        val roomDirectoryList = remember {
            roomDirectoryService.createRoomDirectoryList(coroutineScope)
        }
        val listState by roomDirectoryList.collectState()
        LaunchedEffect(searchQuery) {
            if (searchQuery == null) return@LaunchedEffect
            // cancel load more right away
            loadingMore = false
            // debounce search query
            delay(300)
            roomDirectoryList.filter(filter = searchQuery, batchSize = SEARCH_BATCH_SIZE, viaServerName = null)
        }
        LaunchedEffect(loadingMore) {
            if (loadingMore) {
                roomDirectoryList.loadMore()
                loadingMore = false
            }
        }
        fun handleEvent(event: RoomDirectoryEvents) {
            when (event) {
                RoomDirectoryEvents.LoadMore -> {
                    loadingMore = true
                }
                is RoomDirectoryEvents.Search -> {
                    searchQuery = event.query
                }
            }
        }

        return RoomDirectoryState(
            query = searchQuery.orEmpty(),
            roomDescriptions = listState.items,
            displayLoadMoreIndicator = listState.hasMoreToLoad,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun RoomDirectoryList.collectState() = remember {
        state.map {
            val items = it.items
                .map { roomDescription -> roomDescription.toFeatureModel() }
                .toImmutableList()
            RoomDirectoryListState(items = items, hasMoreToLoad = it.hasMoreToLoad)
        }.flowOn(dispatchers.computation)
    }.collectAsState(RoomDirectoryListState.Default)
}
