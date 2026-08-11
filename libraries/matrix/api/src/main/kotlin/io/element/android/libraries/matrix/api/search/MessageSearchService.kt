/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope

interface MessageSearchService {
    /**
     * Create a new, independent search cursor.
     *
     * The underlying SDK service is stateful (one query at a time), so create one per search
     * screen — never one per keystroke, and never a shared singleton.
     *
     * @param scope the lifetime of the search session. The returned [MessageSearch] observes and
     * emits results on this scope, and the underlying SDK service is closed once the scope
     * completes, so there is nothing to release by hand. Pass the scope of the screen that owns the
     * search, not a longer-lived one.
     * @param roomId when null, the search is session-wide and results from every room the user is
     * in are exposed; when non-null, only results from that room are. Note that the SDK searches
     * and ranks across every room regardless, so this is a client-side filter, not a pushdown: a
     * room with few matches may need several [MessageSearch.paginate] calls before any of its
     * results surface.
     */
    fun createMessageSearch(scope: CoroutineScope, roomId: RoomId? = null): MessageSearch
}
