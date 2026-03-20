/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommessagesearch.impl

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList

data class RoomMessageSearchState(
    val query: String,
    val searchResults: AsyncData<SearchResults>,
    val isEncryptedRoom: Boolean,
    val eventSink: (RoomMessageSearchEvents) -> Unit,
)

data class SearchResults(
    val items: ImmutableList<SearchResultItemState>,
    val count: Long?,
    val hasMore: Boolean,
)

data class SearchResultItemState(
    val eventId: EventId,
    val senderId: UserId,
    val senderName: String?,
    val senderAvatar: AvatarData,
    val body: String,
    val highlights: ImmutableList<String>,
    val formattedDate: String,
)
