/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList

data class MessageSearchState(
    val query: String,
    val results: ImmutableList<MessageSearchResultItem>,
    /** A keystroke has been made but the debounced query has not been handed to the SDK yet. */
    val isSearching: Boolean,
    val isPaginating: Boolean,
    val endReached: Boolean,
    /** True when this search is limited to a single room. */
    val isRoomScoped: Boolean,
    /** True when setting the query or loading another page failed. */
    val hasError: Boolean,
    val eventSink: (MessageSearchEvents) -> Unit,
) {
    /** Nothing has been typed yet — prompt rather than claim there are no results. */
    val displayInitialState: Boolean = query.isBlank() && !hasError

    val displayErrorState: Boolean = hasError

    /** The query genuinely produced nothing and there is nothing left to load. */
    val displayEmptyState: Boolean = query.isNotBlank() &&
        results.isEmpty() &&
        !hasError &&
        !isSearching &&
        !isPaginating &&
        endReached

    /**
     * Nothing to show yet, but the search is still working. A room-scoped search walks the whole
     * globally-ranked result set on its own, so any empty moment before [endReached] belongs to
     * that walk — claiming "no results" or asking the user to load more would both be dishonest.
     */
    val displaySearchingState: Boolean = query.isNotBlank() &&
        results.isEmpty() &&
        !hasError &&
        !endReached &&
        (isSearching || isPaginating || isRoomScoped)

    /**
     * Global search only: results are empty, pages remain and nothing is in flight. A room-scoped
     * search never rests in this state — its walk continues to the end on its own.
     */
    val displayKeepLoadingPrompt: Boolean = query.isNotBlank() &&
        results.isEmpty() &&
        !hasError &&
        !isSearching &&
        !isPaginating &&
        !endReached &&
        !isRoomScoped

    /**
     * The SDK still has pages for this query. Room-scoped searches filter a globally-ranked set, so
     * a page can add nothing to this room's list and more pages still be worth pulling — which is
     * why this deliberately says nothing about how many rows the last page contributed.
     */
    val canLoadMore: Boolean = results.isNotEmpty() && !endReached && !hasError

    /**
     * A page is genuinely in flight. Gated on [isPaginating] rather than on "more pages exist":
     * an indeterminate spinner parked under a list that is not loading anything reads as a hang,
     * and was reported as one.
     */
    val displayLoadMoreIndicator: Boolean = canLoadMore && isPaginating
}

data class MessageSearchResultItem(
    val roomId: RoomId,
    val eventId: EventId,
    val senderAvatarData: AvatarData,
    val senderName: String,
    val preview: String,
    val formattedDate: String,
)
