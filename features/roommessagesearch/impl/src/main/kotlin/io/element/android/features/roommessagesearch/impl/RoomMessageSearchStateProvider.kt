/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommessagesearch.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.persistentListOf

class RoomMessageSearchStateProvider : PreviewParameterProvider<RoomMessageSearchState> {
    override val values: Sequence<RoomMessageSearchState> = sequenceOf(
        // Initial state
        aRoomMessageSearchState(),
        // Loading
        aRoomMessageSearchState(
            query = "hello",
            searchResults = AsyncData.Loading(),
        ),
        // Results
        aRoomMessageSearchState(
            query = "hello",
            searchResults = AsyncData.Success(
                SearchResults(
                    items = persistentListOf(
                        aSearchResultItemState(body = "Hello world! This is a test message."),
                        aSearchResultItemState(
                            senderName = "Bob",
                            body = "Hello everyone, welcome to the room.",
                        ),
                    ),
                    count = 2,
                    hasMore = true,
                )
            ),
        ),
        // Empty results
        aRoomMessageSearchState(
            query = "xyznotfound",
            searchResults = AsyncData.Success(
                SearchResults(
                    items = persistentListOf(),
                    count = 0,
                    hasMore = false,
                )
            ),
        ),
        // Error
        aRoomMessageSearchState(
            query = "hello",
            searchResults = AsyncData.Failure(Exception("Network error")),
        ),
        // Encrypted room
        aRoomMessageSearchState(
            isEncryptedRoom = true,
        ),
    )
}

internal fun aRoomMessageSearchState(
    query: String = "",
    searchResults: AsyncData<SearchResults> = AsyncData.Uninitialized,
    isEncryptedRoom: Boolean = false,
    eventSink: (RoomMessageSearchEvents) -> Unit = {},
) = RoomMessageSearchState(
    query = query,
    searchResults = searchResults,
    isEncryptedRoom = isEncryptedRoom,
    eventSink = eventSink,
)

internal fun aSearchResultItemState(
    eventId: EventId = EventId("\$event1"),
    senderId: UserId = UserId("@alice:matrix.org"),
    senderName: String? = "Alice",
    body: String = "Hello world!",
    formattedDate: String = "12:30 PM",
) = SearchResultItemState(
    eventId = eventId,
    senderId = senderId,
    senderName = senderName,
    senderAvatar = AvatarData(
        id = senderId.value,
        name = senderName,
        size = AvatarSize.TimelineSender,
    ),
    body = body,
    highlights = persistentListOf("hello"),
    formattedDate = formattedDate,
)
