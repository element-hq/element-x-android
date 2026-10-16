/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class GlobalSearchState(
    val isEnabled: Boolean,
    val isSearchActive: Boolean,
    val queryState: TextFieldState,
    val currentTarget: GlobalSearchTarget,
    val results: AsyncData<GlobalSearchResults>,
    val history: AsyncData<ImmutableList<SearchHistoryListItem>>,
    val eventSink: (GlobalSearchEvent) -> Unit
)

/**
 * The target of the global search: either rooms or messages.
 */
@Immutable
enum class GlobalSearchTarget {
    ROOMS,
    MESSAGES,
}

/**
 * The results of a global search, either a list of rooms or a list of messages.
 */
@Immutable
sealed interface GlobalSearchResults {
    data class RoomListResults(val results: ImmutableList<RoomListRoomSummary>) : GlobalSearchResults
    data class MessageSearchResults(val results: ImmutableList<MessageSearchListItem>) : GlobalSearchResults

    fun isEmpty(): Boolean = when (this) {
        is RoomListResults -> results.isEmpty()
        is MessageSearchResults -> results.isEmpty()
    }
}

/**
 * A single item in the message search results. It's the UI representation of [MessageSearchResult], which is the mapped model returned by the Matrix SDK.
 */
sealed interface MessageSearchListItem {
    val roomId: RoomId
    val eventId: EventId
    val senderId: UserId
    val senderName: String
    val roomInfo: RoomInfo
    val formattedTimestamp: String

    /**
     * A message item in the message search results.
     */
    data class Message(
        val messageSearchResult: MessageSearchResult,
        val body: String,
        override val roomInfo: RoomInfo,
        override val formattedTimestamp: String,
    ) : MessageSearchListItem {
        override val roomId: RoomId = messageSearchResult.roomId
        override val eventId: EventId = messageSearchResult.eventId
        override val senderId: UserId = messageSearchResult.senderId
        override val senderName: String = messageSearchResult.senderProfile.getDisambiguatedDisplayName(senderId)
    }

    /**
     * A message with media item in the message search results.
     */
    data class Media(
        val messageSearchResult: MessageSearchResult,
        val mediaContent: MediaSearchResultContent,
        override val roomInfo: RoomInfo,
        override val formattedTimestamp: String,
    ) : MessageSearchListItem {
        override val roomId: RoomId = messageSearchResult.roomId
        override val eventId: EventId = messageSearchResult.eventId
        override val senderId: UserId = messageSearchResult.senderId
        override val senderName: String = messageSearchResult.senderProfile.getDisambiguatedDisplayName(senderId)
    }
}

/**
 * A single entry in the global search history, displayed in the list of recent searches.
 *
 * It's the UI representation of [io.element.android.features.home.impl.search.history.SearchHistoryResult], which is the persisted model.
 */
sealed interface SearchHistoryListItem {
    val id: String

    data class Query(val term: String) : SearchHistoryListItem {
        override val id: String = "query:$term"
    }
    data class Room(
        val roomId: RoomId,
        val roomInfo: RoomInfo,
    ) : SearchHistoryListItem {
        override val id: String = "room:${roomId.value}"
    }
}

/**
 * Represents the media content of a message search result.
 */
data class MediaSearchResultContent(
    val filename: String,
    val extension: String?,
    val caption: String?,
    val formattedSize: String?,
    val thumbnailSource: MediaSource?,
    val thumbnailType: AttachmentThumbnailType,
    val blurhash: String?,
)
