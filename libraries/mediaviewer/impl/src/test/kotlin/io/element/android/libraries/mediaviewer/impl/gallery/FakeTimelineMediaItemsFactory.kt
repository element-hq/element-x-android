/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTimelineMediaItemsFactory(
    private val replaceWithLambda: (List<MatrixTimelineItem>) -> Unit = { lambdaError() },
    private val onCanPaginateLambda: () -> Unit = { lambdaError() }
) : TimelineMediaItemsFactory {
    override val timelineItems: Flow<ImmutableList<MediaItem>>
        get() = flowOf(emptyList<MediaItem>().toImmutableList())

    override suspend fun replaceWith(timelineItems: List<MatrixTimelineItem>) {
        replaceWithLambda(timelineItems)
    }

    override suspend fun onCanPaginate() {
        onCanPaginateLambda()
    }
}
