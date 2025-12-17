/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeRoomDirectoryList(
    override val state: Flow<RoomDirectoryList.SearchResult> = emptyFlow(),
    val filterLambda: (String?, Int, String?) -> Result<Unit> = { _, _, _ -> Result.success(Unit) },
    val loadMoreLambda: () -> Result<Unit> = { Result.success(Unit) }
) : RoomDirectoryList {
    override suspend fun filter(filter: String?, batchSize: Int, viaServerName: String?): Result<Unit> = filterLambda(filter, batchSize, viaServerName)

    override suspend fun loadMore(): Result<Unit> = loadMoreLambda()
}
