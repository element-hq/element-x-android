/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

/**
 * How many positions away from the first or last item in the viewport a loading indicator triggers a pagination.
 */
private const val LOADING_INDICATOR_DISTANCE = 5

/**
 * Paginates when a loading indicator is close to the viewport, i.e. at [LOADING_INDICATOR_DISTANCE] positions or less from its first or last item.
 *
 * Unlike paginating when the loading indicator is composed, this ignores the items the LazyColumn composes out of the viewport, like a loading
 * indicator moving out of the screen with its placement animation, which would paginate again and again.
 *
 * A loading indicator paginates when it comes close to the viewport, and again each time its timestamp changes while it stays close, i.e. when a
 * pagination ended without bringing new events.
 */
@Composable
internal fun PaginateNearLoadingIndicators(
    lazyListState: LazyListState,
    timelineItems: ImmutableList<TimelineItem>,
    onPaginate: (Timeline.PaginationDirection) -> Unit,
) {
    val latestTimelineItems by rememberUpdatedState(timelineItems)
    val latestOnPaginate by rememberUpdatedState(onPaginate)
    LaunchedEffect(lazyListState) {
        var previousLoadingIndicators = emptySet<TimelineItemLoadingIndicatorModel>()
        snapshotFlow { latestTimelineItems.loadingIndicatorsNearViewport(lazyListState.layoutInfo, LOADING_INDICATOR_DISTANCE) }
            .filterNotNull()
            .collect { loadingIndicators ->
                for ((direction) in loadingIndicators - previousLoadingIndicators) {
                    Timber.d("Pagination triggered by the $direction loading indicator")
                    latestOnPaginate(direction)
                }
                previousLoadingIndicators = loadingIndicators
            }
    }
}

/**
 * Returns the loading indicators at [distance] positions or less from the first or last item in the viewport, or `null` if the layout info doesn't
 * match these items yet.
 */
internal fun List<TimelineItem>.loadingIndicatorsNearViewport(layoutInfo: LazyListLayoutInfo, distance: Int): Set<TimelineItemLoadingIndicatorModel>? {
    // Not the items out of the viewport, like the items moving out of it with their placement animation, which report their target position
    val itemsInViewport = layoutInfo.visibleItemsInfo.filter {
        it.offset < layoutInfo.viewportEndOffset && it.offset + it.size > layoutInfo.viewportStartOffset
    }
    val first = itemsInViewport.minByOrNull { it.index } ?: return emptySet()
    val last = itemsInViewport.maxBy { it.index }
    // The items are updated before the layout info: indexes from a previous layout could match the wrong items
    if (getOrNull(first.index)?.identifier() != first.key || getOrNull(last.index)?.identifier() != last.key) return null
    val nearIndexes = (first.index - distance).coerceAtLeast(0)..(last.index + distance).coerceAtMost(lastIndex)
    return nearIndexes.mapNotNullTo(HashSet()) { index ->
        (get(index) as? TimelineItem.Virtual)?.model as? TimelineItemLoadingIndicatorModel
    }
}
