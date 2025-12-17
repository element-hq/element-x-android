/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.poll.api.pollcontent.PollContentState
import io.element.android.features.poll.api.pollcontent.aPollContentState
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItem
import io.element.android.features.poll.impl.history.model.PollHistoryItems
import kotlinx.collections.immutable.toImmutableList

class PollHistoryStateProvider : PreviewParameterProvider<PollHistoryState> {
    override val values: Sequence<PollHistoryState>
        get() = sequenceOf(
            aPollHistoryState(),
            aPollHistoryState(
                isLoading = true,
                hasMoreToLoad = true,
                activeFilter = PollHistoryFilter.PAST,
            ),
            aPollHistoryState(
                activeFilter = PollHistoryFilter.ONGOING,
                currentItems = emptyList(),
            ),
            aPollHistoryState(
                activeFilter = PollHistoryFilter.PAST,
                currentItems = emptyList(),
            ),
            aPollHistoryState(
                activeFilter = PollHistoryFilter.PAST,
                currentItems = emptyList(),
                hasMoreToLoad = true,
            ),
        )
}

internal fun aPollHistoryState(
    isLoading: Boolean = false,
    hasMoreToLoad: Boolean = false,
    activeFilter: PollHistoryFilter = PollHistoryFilter.ONGOING,
    currentItems: List<PollHistoryItem> = listOf(
        aPollHistoryItem(),
    ),
    eventSink: (PollHistoryEvents) -> Unit = {},
) = PollHistoryState(
    isLoading = isLoading,
    hasMoreToLoad = hasMoreToLoad,
    activeFilter = activeFilter,
    pollHistoryItems = PollHistoryItems(
        ongoing = currentItems.toImmutableList(),
        past = currentItems.toImmutableList(),
    ),
    eventSink = eventSink,
)

internal fun aPollHistoryItem(
    formattedDate: String = "01/12/2023",
    state: PollContentState = aPollContentState(),
) = PollHistoryItem(
    formattedDate = formattedDate,
    state = state,
)
