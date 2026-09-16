/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MessageSearch
import io.element.android.libraries.matrix.api.search.MessageSearchService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.matrix.rustcomponents.sdk.Client

class RustMessageSearchService(
    private val client: Client,
    private val sessionDispatcher: CoroutineDispatcher,
) : MessageSearchService {
    override fun createMessageSearch(scope: CoroutineScope, roomId: RoomId?): MessageSearch {
        // Each searchService() call mints a new Rust object, so this instance owns the one it
        // creates and closes it when the caller's scope completes.
        val searchService = client.searchService()
        return RustMessageSearch(
            inner = searchService,
            onClose = { searchService.close() },
            scope = scope,
            dispatcher = sessionDispatcher,
            roomId = roomId,
        )
    }
}
