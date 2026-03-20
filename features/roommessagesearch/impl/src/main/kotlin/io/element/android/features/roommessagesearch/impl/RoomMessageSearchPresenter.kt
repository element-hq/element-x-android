/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommessagesearch.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.search.RoomMessageSearchResult
import io.element.android.libraries.matrix.api.search.RoomMessageSearchService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

private const val MIN_QUERY_LENGTH = 2
private const val SEARCH_DEBOUNCE_MS = 300L
private const val SEARCH_BATCH_SIZE = 10

@Inject
class RoomMessageSearchPresenter(
    private val room: JoinedRoom,
    private val roomMessageSearchService: RoomMessageSearchService,
    private val dateFormatter: DateFormatter,
) : Presenter<RoomMessageSearchState> {
    @Composable
    override fun present(): RoomMessageSearchState {
        var query by rememberSaveable { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<AsyncData<SearchResults>>(AsyncData.Uninitialized) }
        var accumulatedItems by remember { mutableStateOf<ImmutableList<SearchResultItemState>>(persistentListOf()) }
        var nextBatchToken by remember { mutableStateOf<String?>(null) }
        var highlights by remember { mutableStateOf<ImmutableList<String>>(persistentListOf()) }
        var loadMoreRequested by remember { mutableStateOf(false) }
        var retryRequested by remember { mutableStateOf(false) }

        val roomInfo by room.roomInfoFlow.collectAsState()
        val isEncrypted = roomInfo.isEncrypted == true

        LaunchedEffect(query) {
            if (query.length < MIN_QUERY_LENGTH) {
                searchResults = AsyncData.Uninitialized
                accumulatedItems = persistentListOf()
                nextBatchToken = null
                highlights = persistentListOf()
                return@LaunchedEffect
            }
            // Reset for new query
            accumulatedItems = persistentListOf()
            nextBatchToken = null
            highlights = persistentListOf()
            searchResults = AsyncData.Loading()
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(
                query = query,
                nextBatchToken = null,
                onResult = { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            val items = mapToItems(searchResult)
                            accumulatedItems = items.toImmutableList()
                            nextBatchToken = searchResult.nextBatchToken
                            highlights = searchResult.highlights.toImmutableList()
                            searchResults = AsyncData.Success(
                                SearchResults(
                                    items = accumulatedItems,
                                    count = searchResult.count,
                                    hasMore = searchResult.nextBatchToken != null,
                                )
                            )
                        },
                        onFailure = {
                            searchResults = AsyncData.Failure(it)
                        }
                    )
                }
            )
        }

        LaunchedEffect(loadMoreRequested) {
            if (!loadMoreRequested) return@LaunchedEffect
            loadMoreRequested = false
            val currentToken = nextBatchToken ?: return@LaunchedEffect
            performSearch(
                query = query,
                nextBatchToken = currentToken,
                onResult = { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            val newItems = mapToItems(searchResult)
                            accumulatedItems = (accumulatedItems + newItems).toImmutableList()
                            nextBatchToken = searchResult.nextBatchToken
                            searchResults = AsyncData.Success(
                                SearchResults(
                                    items = accumulatedItems,
                                    count = searchResult.count,
                                    hasMore = searchResult.nextBatchToken != null,
                                )
                            )
                        },
                        onFailure = {
                            // Keep existing results on load more failure
                        }
                    )
                }
            )
        }

        LaunchedEffect(retryRequested) {
            if (!retryRequested) return@LaunchedEffect
            retryRequested = false
            searchResults = AsyncData.Loading()
            performSearch(
                query = query,
                nextBatchToken = null,
                onResult = { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            val items = mapToItems(searchResult)
                            accumulatedItems = items.toImmutableList()
                            nextBatchToken = searchResult.nextBatchToken
                            highlights = searchResult.highlights.toImmutableList()
                            searchResults = AsyncData.Success(
                                SearchResults(
                                    items = accumulatedItems,
                                    count = searchResult.count,
                                    hasMore = searchResult.nextBatchToken != null,
                                )
                            )
                        },
                        onFailure = {
                            searchResults = AsyncData.Failure(it)
                        }
                    )
                }
            )
        }

        fun handleEvent(event: RoomMessageSearchEvents) {
            when (event) {
                is RoomMessageSearchEvents.UpdateQuery -> {
                    query = event.query
                }
                RoomMessageSearchEvents.LoadMore -> {
                    loadMoreRequested = true
                }
                RoomMessageSearchEvents.RetrySearch -> {
                    retryRequested = true
                }
            }
        }

        return RoomMessageSearchState(
            query = query,
            searchResults = searchResults,
            isEncryptedRoom = isEncrypted,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun performSearch(
        query: String,
        nextBatchToken: String?,
        onResult: (Result<RoomMessageSearchResult>) -> Unit,
    ) {
        val result = roomMessageSearchService.search(
            roomId = room.roomId,
            searchTerm = query,
            batchSize = SEARCH_BATCH_SIZE,
            nextBatchToken = nextBatchToken,
        )
        onResult(result)
    }

    private fun mapToItems(searchResult: RoomMessageSearchResult): List<SearchResultItemState> {
        return searchResult.results.map { item ->
            SearchResultItemState(
                eventId = item.eventId,
                senderId = item.senderId,
                senderName = item.senderDisplayName,
                senderAvatar = AvatarData(
                    id = item.senderId.value,
                    name = item.senderDisplayName,
                    url = item.senderAvatarUrl,
                    size = AvatarSize.TimelineSender,
                ),
                body = item.body,
                highlights = searchResult.highlights.toImmutableList(),
                formattedDate = dateFormatter.format(
                    timestamp = item.timestamp,
                    mode = DateFormatterMode.TimeOrDate,
                    useRelative = true,
                ),
            )
        }
    }
}
