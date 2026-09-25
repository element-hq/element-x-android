/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.serialization.Serializable

/**
 * A single entry in the global search history.
 *
 * The history stores either the term used to perform a search (when the keyboard search action is
 * triggered) or the id of a room opened from the search results.
 */
@Serializable
sealed interface SearchHistoryResult {
    @Serializable
    data class Query(val term: String) : SearchHistoryResult

    @Serializable
    data class Room(val roomId: RoomId) : SearchHistoryResult
}
