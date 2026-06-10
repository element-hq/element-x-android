/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.floatingDateBadgeBackground
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun BoxScope.FloatingDateBadgeOverlay(
    lazyListState: LazyListState,
    timelineItems: ImmutableList<TimelineItem>,
    isLive: Boolean,
    topOffset: Dp = 0.dp,
) {
    // This needs to be a state to trigger a `derivedState` recalculation
    val updatedTimelineItems by rememberUpdatedState(timelineItems)

    // Look for the last visible item with a timestamp, starting from the last visible item and going backwards until we find one or reach the start of the list
    val lastVisibleItemWithTimestamp by remember {
        derivedStateOf {
            var index = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf null
            while (index >= 0) {
                when (val item = updatedTimelineItems.getOrNull(index)) {
                    is TimelineItem.Event -> return@derivedStateOf item
                    is TimelineItem.Virtual -> if (item.model is TimelineItemDaySeparatorModel) return@derivedStateOf item
                    is TimelineItem.GroupedEvents -> return@derivedStateOf item.events.firstOrNull()
                    null -> Unit
                }
                index--
            }
            null
        }
    }

    // Store the formatted date so we recompute it lazily and can keep it around even if we need to dispose the badge because the timeline items changed
    var formattedDate: String? by remember { mutableStateOf(null) }
    // Update the formatted date when we have a new non-null timestamp
    LaunchedEffect(lastVisibleItemWithTimestamp) {
        lastVisibleItemWithTimestamp?.formattedDate()?.let { formattedDate = it }
    }

    val isAtBottom by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < 3 && isLive
        }
    }

    var isBadgeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    isBadgeVisible = true
                } else {
                    delay(2000.milliseconds)
                    isBadgeVisible = false
                }
            }
    }

    val showBadge = isBadgeVisible && !isAtBottom && formattedDate != null

    AnimatedVisibility(
        visible = showBadge,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp + topOffset),
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(300)),
    ) {
        formattedDate?.let { dateText ->
            FloatingDateBadge(
                modifier = Modifier.padding(8.dp),
                dateText = dateText,
            )
        }
    }
}

@Composable
internal fun FloatingDateBadge(
    dateText: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ElementTheme.colors.floatingDateBadgeBackground,
        shadowElevation = 4.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = dateText,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun FloatingDateBadgePreview() = ElementPreview {
    Box(modifier = Modifier.padding(16.dp)) {
        FloatingDateBadge(dateText = "March 9, 2026")
    }
}
