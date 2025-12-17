/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItem
import io.element.android.features.poll.impl.history.model.PollHistoryItems
import kotlinx.collections.immutable.ImmutableList

data class PollHistoryState(
    val isLoading: Boolean,
    val hasMoreToLoad: Boolean,
    val activeFilter: PollHistoryFilter,
    val pollHistoryItems: PollHistoryItems,
    val eventSink: (PollHistoryEvents) -> Unit,
) {
    fun pollHistoryForFilter(filter: PollHistoryFilter): ImmutableList<PollHistoryItem> {
        return when (filter) {
            PollHistoryFilter.ONGOING -> pollHistoryItems.ongoing
            PollHistoryFilter.PAST -> pollHistoryItems.past
        }
    }
}
