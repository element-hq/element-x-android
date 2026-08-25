/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

/**
 * Whether the search is currently loading a page of results.
 */
sealed interface MessageSearchPaginationState {
    /**
     * Not currently paginating. [endReached] is true once every source has been
     * exhausted for the current query.
     */
    data class Idle(val endReached: Boolean) : MessageSearchPaginationState

    /**
     * A page of results is currently being loaded.
     */
    data object Loading : MessageSearchPaginationState
}
