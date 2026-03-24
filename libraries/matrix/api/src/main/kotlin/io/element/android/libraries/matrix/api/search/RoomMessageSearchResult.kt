/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

data class RoomMessageSearchResult(
    val results: List<RoomMessageSearchResultItem>,
    val count: Long?,
    val highlights: List<String>,
    val nextBatchToken: String?,
)

data class RoomMessageSearchResultItem(
    val eventId: EventId,
    val senderId: UserId,
    val senderDisplayName: String?,
    val senderAvatarUrl: String?,
    val body: String,
    val timestamp: Long,
)
