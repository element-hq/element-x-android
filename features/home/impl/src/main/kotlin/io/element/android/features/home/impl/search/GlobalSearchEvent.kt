/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface GlobalSearchEvent {
    /** The user cleared the search query. */
    object ClearQuery : GlobalSearchEvent

    /** The search screen visibility changed (e.g. the user opened or closed the search screen). */
    object ToggleSearchVisibility : GlobalSearchEvent

    /** The visible range for the displayed search results changed. */
    data class UpdateVisibleRange(val range: IntRange) : GlobalSearchEvent

    /** The user changed the search target (e.g. from "Rooms" to "Messages"). */
    data class UpdateTarget(val target: GlobalSearchTarget) : GlobalSearchEvent

    /** The user triggered the keyboard search action (enter key): save the current query to the search history. */
    object SaveQueryToHistory : GlobalSearchEvent

    /** The user opened a room from the search results: save it to the search history. */
    data class SaveRoomToHistory(val roomId: RoomId) : GlobalSearchEvent

    /** The user selected a search result from the search history. */
    data class SearchHistoryResultSelected(val resultItem: SearchHistoryResultItem) : GlobalSearchEvent
}
