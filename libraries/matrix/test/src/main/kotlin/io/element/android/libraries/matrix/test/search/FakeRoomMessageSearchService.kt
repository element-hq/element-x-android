/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.search

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.RoomMessageSearchResult
import io.element.android.libraries.matrix.api.search.RoomMessageSearchService
import io.element.android.libraries.matrix.api.search.SearchOrder

class FakeRoomMessageSearchService(
    var searchResult: Result<RoomMessageSearchResult> = Result.success(
        RoomMessageSearchResult(
            results = emptyList(),
            count = 0,
            highlights = emptyList(),
            nextBatchToken = null,
        )
    ),
) : RoomMessageSearchService {
    override suspend fun search(
        roomId: RoomId,
        searchTerm: String,
        batchSize: Int,
        nextBatchToken: String?,
        orderBy: SearchOrder,
    ): Result<RoomMessageSearchResult> = searchResult
}
