/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeRoomDirectoryList(
    override val state: Flow<RoomDirectoryList.State> = emptyFlow(),
    val filterLambda: (String?, Int) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    val loadMoreLambda: () -> Result<Unit> = { Result.success(Unit) }
) : RoomDirectoryList {
    override suspend fun filter(filter: String?, batchSize: Int) = filterLambda(filter, batchSize)

    override suspend fun loadMore(): Result<Unit> = loadMoreLambda()
}
