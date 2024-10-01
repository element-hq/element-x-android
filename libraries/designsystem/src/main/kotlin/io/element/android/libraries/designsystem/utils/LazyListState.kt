/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Returns whether the lazy list is currently scrolling up.
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

suspend fun LazyListState.animateScrollToItemCenter(index: Int) {
    fun LazyListLayoutInfo.containerSize(): Int {
        return if (orientation == Orientation.Vertical) {
            viewportSize.height
        } else {
            viewportSize.width
        } - beforeContentPadding - afterContentPadding
    }

    fun LazyListLayoutInfo.resolveItemOffsetToCenter(index: Int): Int? {
        val itemInfo = visibleItemsInfo.firstOrNull { it.index == index } ?: return null
        val containerSize = containerSize()
        val itemSize = itemInfo.size
        return if (itemSize > containerSize) {
            itemSize - containerSize / 2
        } else {
            -(containerSize() - itemInfo.size) / 2
        }
    }

    // await for the first layout.
    scroll { }
    layoutInfo.resolveItemOffsetToCenter(index)?.let { offset ->
        // Item is already visible, just scroll to center.
        animateScrollToItem(index, offset)
        return
    }
    // Item is not visible, jump to it...
    scrollToItem(index)
    // and then adjust according to the actual item size.
    layoutInfo.resolveItemOffsetToCenter(index)?.let { offset ->
        animateScrollToItem(index, offset)
    }
}
