/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class MessageSearchStatePreviewParam : PreviewParameterProvider<MessageSearchState> {
    override val values: Sequence<MessageSearchState>
        get() = sequenceOf(
            // Nothing typed yet.
            aMessageSearchState(),
            // Typing, first search still pending.
            aMessageSearchState(query = "hello", isSearching = true),
            // Results.
            aMessageSearchState(query = "hello", results = aMessageSearchResultItemList(), endReached = true),
            // Results, with the next page on its way. The footer spinner is tied to a page being in
            // flight, so this is the only state that renders it.
            aMessageSearchState(query = "hello", results = aMessageSearchResultItemList(), isPaginating = true),
            // Genuinely nothing found.
            aMessageSearchState(query = "hello", endReached = true),
            // Search failed.
            aMessageSearchState(query = "hello", hasError = true),
            // Room-scoped, nothing to show yet while the walk over the global results runs.
            aMessageSearchState(query = "hello", isRoomScoped = true),
        )
}

fun aMessageSearchState(
    query: String = "",
    results: ImmutableList<MessageSearchResultItem> = persistentListOf(),
    isSearching: Boolean = false,
    isPaginating: Boolean = false,
    endReached: Boolean = false,
    isRoomScoped: Boolean = false,
    hasError: Boolean = false,
    eventSink: (MessageSearchEvents) -> Unit = {},
) = MessageSearchState(
    query = query,
    results = results,
    isSearching = isSearching,
    isPaginating = isPaginating,
    endReached = endReached,
    isRoomScoped = isRoomScoped,
    hasError = hasError,
    eventSink = eventSink,
)

fun aMessageSearchResultItem(
    eventId: EventId = EventId("\$anEventId"),
    senderName: String = "Alice",
    preview: String = "Hello world, this is a message that matched the search query",
    formattedDate: String = "12:34",
) = MessageSearchResultItem(
    roomId = RoomId("!aRoomId:domain"),
    eventId = eventId,
    senderAvatarData = AvatarData(
        id = "@alice:domain",
        name = senderName,
        url = null,
        size = AvatarSize.UserListItem,
    ),
    senderName = senderName,
    preview = preview,
    formattedDate = formattedDate,
)

fun aMessageSearchResultItemList() = listOf(
    aMessageSearchResultItem(),
    aMessageSearchResultItem(
        eventId = EventId("\$anotherEventId"),
        senderName = "Bob",
        preview = "Another matching message",
        formattedDate = "Yesterday",
    ),
).toImmutableList()
