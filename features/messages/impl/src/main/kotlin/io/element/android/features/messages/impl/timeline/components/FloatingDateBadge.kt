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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun BoxScope.FloatingDateBadgeOverlay(
    lazyListState: LazyListState,
    timelineItems: ImmutableList<TimelineItem>,
    isLive: Boolean,
    useReverseLayout: Boolean,
    topOffset: Dp = 0.dp,
) {
    val currentDateText by remember(timelineItems) {
        derivedStateOf {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf null

            // In reverse layout, the last visible item is at the top of the screen
            val topVisibleIndex = if (useReverseLayout) {
                visibleItems.last().index
            } else {
                visibleItems.first().index
            }

            // Search forward (toward older items) for the nearest day separator
            for (i in topVisibleIndex until timelineItems.size) {
                val item = timelineItems[i]
                if (item is TimelineItem.Virtual && item.model is TimelineItemDaySeparatorModel) {
                    return@derivedStateOf item.model.formattedDate
                }
            }
            // Fallback: search backward (toward newer items)
            for (i in topVisibleIndex - 1 downTo 0) {
                val item = timelineItems[i]
                if (item is TimelineItem.Virtual && item.model is TimelineItemDaySeparatorModel) {
                    return@derivedStateOf item.model.formattedDate
                }
            }
            null
        }
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

    val showBadge = isBadgeVisible && !isAtBottom && currentDateText != null

    AnimatedVisibility(
        visible = showBadge,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp + topOffset),
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(300)),
    ) {
        currentDateText?.let { dateText ->
            FloatingDateBadge(dateText = dateText)
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
        color = if (ElementTheme.isLightTheme) {
            ElementTheme.colors.bgCanvasDefault.copy(alpha = 0.85f)
        } else {
            ElementTheme.colors.bgSubtlePrimary
        },
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
    FloatingDateBadge(dateText = "March 9, 2026")
}
