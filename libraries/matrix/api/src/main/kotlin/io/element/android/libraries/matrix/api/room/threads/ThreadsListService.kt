/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.threads

import kotlinx.coroutines.flow.Flow

interface ThreadsListService {
    fun subscribeToItemUpdates(): Flow<List<ThreadListItem>>
    fun subscribeToPaginationUpdates(): Flow<ThreadListPaginationStatus>
    suspend fun paginate(): Result<Unit>
    suspend fun reset(): Result<Unit>
    fun destroy()
}
