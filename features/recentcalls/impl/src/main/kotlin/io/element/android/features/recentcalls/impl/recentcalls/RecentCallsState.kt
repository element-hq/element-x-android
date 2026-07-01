/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.recentcalls

import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallsFilter
import kotlinx.collections.immutable.ImmutableList

data class RecentCallsState(
    val filter: RecentCallsFilter,
    val entries: ImmutableList<RecentCallEntry>,
    val isLoading: Boolean,
    val canLoadMore: Boolean,
    val isLoadingMore: Boolean,
    val eventSink: (RecentCallsEvent) -> Unit,
)
