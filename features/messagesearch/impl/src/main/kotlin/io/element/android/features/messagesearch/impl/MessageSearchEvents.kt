/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

sealed interface MessageSearchEvents {
    data class QueryChanged(val query: String) : MessageSearchEvents

    /**
     * Load another page. Only reachable from the global search's empty-with-pages-remaining
     * prompt; a room-scoped search walks the result set to the end on its own.
     */
    data object LoadMore : MessageSearchEvents

    /**
     * The end of the result list is on screen, so the next page is worth fetching. Reported as a
     * state rather than as a "load one more" command: pages must be requested one at a time and
     * awaited, and only the presenter can see when one has finished.
     */
    data class ListEndVisible(val isVisible: Boolean) : MessageSearchEvents
}
