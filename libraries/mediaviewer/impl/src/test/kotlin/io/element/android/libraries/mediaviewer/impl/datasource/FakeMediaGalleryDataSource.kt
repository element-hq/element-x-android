/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeMediaGalleryDataSource(
    private val startLambda: () -> Unit = { lambdaError() },
    private val loadMoreLambda: (Timeline.PaginationDirection) -> Unit = { lambdaError() },
    private val deleteItemLambda: (EventId) -> Unit = { lambdaError() },
    ) : MediaGalleryDataSource {
    override fun start() = startLambda()

    private val groupedMediaItemsFlow = MutableSharedFlow<AsyncData<GroupedMediaItems>>(
        replay = 1
    )

    override fun groupedMediaItemsFlow(): Flow<AsyncData<GroupedMediaItems>> {
        return groupedMediaItemsFlow
    }

    suspend fun emitGroupedMediaItems(groupedMediaItems: AsyncData<GroupedMediaItems>) {
        groupedMediaItemsFlow.emit(groupedMediaItems)
    }

    override fun getLastData(): AsyncData<GroupedMediaItems> {
        return groupedMediaItemsFlow.replayCache.firstOrNull() ?: AsyncData.Uninitialized
    }

    override suspend fun loadMore(direction: Timeline.PaginationDirection) {
        loadMoreLambda(direction)
    }

    override suspend fun deleteItem(eventId: EventId) {
        deleteItemLambda(eventId)
    }
}
