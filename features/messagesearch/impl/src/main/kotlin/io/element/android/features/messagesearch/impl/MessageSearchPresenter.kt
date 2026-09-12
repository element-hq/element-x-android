/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue
import io.element.android.libraries.matrix.api.search.MessageSearchPaginationState
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * iOS uses 250 ms and it feels right; long enough to skip intermediate keystrokes, short enough
 * that the list does not feel laggy.
 */
private const val DEBOUNCE_MILLIS = 250L

@AssistedInject
class MessageSearchPresenter(
    @Assisted private val roomId: RoomId?,
    private val matrixClient: MatrixClient,
    private val roomLatestEventFormatter: RoomLatestEventFormatter,
    private val dateFormatter: DateFormatter,
) : Presenter<MessageSearchState> {
    @AssistedFactory
    fun interface Factory {
        fun create(roomId: RoomId?): MessageSearchPresenter
    }

    @Composable
    override fun present(): MessageSearchState {
        val coroutineScope = rememberCoroutineScope()
        // One cursor per screen. Cancelling the scope releases the underlying Rust service.
        val messageSearch = remember(coroutineScope) {
            matrixClient.messageSearchService.createMessageSearch(coroutineScope, roomId)
        }

        var query by rememberSaveable { mutableStateOf("") }
        var isSearching by remember { mutableStateOf(false) }
        var hasError by remember { mutableStateOf(false) }
        var loadMoreCount by remember { mutableIntStateOf(0) }
        var handledLoadMoreCount by remember { mutableIntStateOf(0) }
        var isListEndVisible by remember { mutableStateOf(false) }

        val results by messageSearch.results.collectAsState()
        val paginationState by messageSearch.paginationState.collectAsState()

        LaunchedEffect(query) {
            // A new query invalidates the previous pagination state.
            loadMoreCount = 0
            handledLoadMoreCount = 0
            hasError = false
            if (query.isBlank()) {
                isSearching = false
                return@LaunchedEffect
            }
            // Flip the indicator on the raw keystroke, not the debounced one, so the empty state
            // does not flash while the first search is still pending.
            isSearching = true
            // Relaunching this effect cancels the previous one, so a still-pending query is
            // superseded by the newer one — the user typing again always wins.
            delay(DEBOUNCE_MILLIS)
            hasError = messageSearch.setQuery(query).isFailure
            isSearching = false
        }

        // Every page this screen ever fetches is pulled by this one loop, one at a time, each call
        // awaited. That serialisation is the point: the View cannot see when a page has landed, so
        // anything that let it ask for pages directly could queue several at once, and — worse —
        // could stop asking after a page that happened to add no rows for this room.
        LaunchedEffect(roomId, query, isSearching, loadMoreCount) {
            if (query.isBlank() || isSearching || hasError) return@LaunchedEffect

            suspend fun paginate(): Boolean {
                val result = messageSearch.paginate()
                hasError = result.isFailure
                return result.isSuccess
            }

            if (loadMoreCount > handledLoadMoreCount) {
                handledLoadMoreCount = loadMoreCount
                if (!paginate()) return@LaunchedEffect
            }

            // Scroll position is collected rather than used as an effect key: as a key, scrolling
            // away would cancel the request already in flight, and a half-issued page leaves the
            // Rust service believing it is still loading. `collect` suspends its own source instead,
            // so a page always finishes and the loop notices the change on its next pass.
            snapshotFlow { isListEndVisible }.collect {
                var keepPaginating = true
                while (keepPaginating) {
                    // Awaited, not sampled. The SDK reports its pagination state through a listener
                    // on its own thread, so it can still read Loading for a moment after paginate()
                    // has returned. Treating that as "stop" would end the search on a race, and
                    // nothing would restart it until the user scrolled away and back.
                    val idle = messageSearch.paginationState
                        .filterIsInstance<MessageSearchPaginationState.Idle>()
                        .first()
                    // Two reasons to pull a page, covering two different screens. A room-scoped
                    // search with nothing to show yet is filtering a globally-ranked set, so it
                    // walks the whole set on its own — the pages come from the local index, and
                    // any one of them could be the one holding this room's hits. A list that
                    // already has rows pulls only while the user is looking at the end of it.
                    val roomScopedAndEmpty = roomId != null && messageSearch.results.value.isEmpty()
                    val shouldPaginate = !idle.endReached && (roomScopedAndEmpty || isListEndVisible)
                    // The loop stops on the first pass that has no reason to pull, or on a failure.
                    keepPaginating = shouldPaginate && paginate()
                }
            }
        }

        val items = remember(results) {
            results.map { it.toResultItem() }.toImmutableList()
        }

        fun handleEvent(event: MessageSearchEvents) {
            when (event) {
                is MessageSearchEvents.QueryChanged -> {
                    query = event.query
                }
                MessageSearchEvents.LoadMore -> {
                    hasError = false
                    loadMoreCount++
                }
                is MessageSearchEvents.ListEndVisible -> {
                    isListEndVisible = event.isVisible
                }
            }
        }

        return MessageSearchState(
            query = query,
            results = items,
            isSearching = isSearching,
            isPaginating = paginationState is MessageSearchPaginationState.Loading,
            endReached = (paginationState as? MessageSearchPaginationState.Idle)?.endReached == true,
            isRoomScoped = roomId != null,
            hasError = hasError,
            eventSink = ::handleEvent,
        )
    }

    private fun MessageSearchResult.toResultItem(): MessageSearchResultItem {
        val senderName = senderProfile.getDisambiguatedDisplayName(senderId)
        return MessageSearchResultItem(
            roomId = roomId,
            eventId = eventId,
            senderAvatarData = AvatarData(
                id = senderId.value,
                name = senderName,
                url = senderProfile.getAvatarUrl(),
                size = AvatarSize.UserListItem,
            ),
            senderName = senderName,
            // RoomLatestEventFormatter, not TimelineEventFormatter: the latter deliberately
            // error()s on MessageContent in debuggable builds, and a search hit is always a message.
            preview = roomLatestEventFormatter.format(
                latestEvent = LatestEventValue.Remote(
                    timestamp = timestamp,
                    content = content,
                    senderId = senderId,
                    senderProfile = senderProfile,
                    isOwn = senderId == matrixClient.sessionId,
                ),
                isDmRoom = false,
            )?.toString().orEmpty(),
            formattedDate = dateFormatter.format(
                timestamp = timestamp,
                mode = DateFormatterMode.TimeOrDate,
                useRelative = true,
            ),
        )
    }
}
