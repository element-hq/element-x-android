/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.features.recentcalls.api.RecentCallsService
import io.element.android.features.recentcalls.impl.cache.RecentCallsCache
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.Flow

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
@Inject
class DefaultRecentCallsService(
    private val cache: RecentCallsCache,
    private val aggregator: RecentCallsAggregator,
) : RecentCallsService {
    override fun recentCalls(filter: RecentCallsFilter): Flow<List<RecentCallEntry>> = cache.recentCalls(filter)

    override val isLoading: Flow<Boolean> = aggregator.isLoading

    override val canLoadMore: Flow<Boolean> = aggregator.canLoadMore

    override suspend fun loadMore() = aggregator.loadMore()
}
