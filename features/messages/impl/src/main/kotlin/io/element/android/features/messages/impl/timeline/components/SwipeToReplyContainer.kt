/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.swipe.SwipeableActionsState
import io.element.android.libraries.designsystem.swipe.rememberSwipeableActionsState
import io.element.android.libraries.designsystem.text.toPx
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Adds swipe-to-reply handling around [content] when [enabled].
 * The [content] receives the modifier making it follow the swipe gesture, and a reply indicator is
 * displayed behind it while swiping.
 */
@Composable
internal fun SwipeToReplyContainer(
    enabled: Boolean,
    onSwipeToReply: () -> Unit,
    content: @Composable (contentModifier: Modifier) -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val content = remember { movableContentOf(content) }
    if (enabled) {
        val coroutineScope = rememberCoroutineScope()
        val state: SwipeableActionsState = rememberSwipeableActionsState()
        val offset = state.offset.floatValue
        val swipeThresholdPx = 40.dp.toPx()
        val thresholdCrossed = abs(offset) > swipeThresholdPx
        SwipeSensitivity(3f) {
            Box(Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.matchParentSize()) {
                    ReplySwipeIndicator({ offset / 120 })
                }
                content(
                    Modifier
                        .absoluteOffset { IntOffset(x = offset.roundToInt(), y = 0) }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            enabled = !state.isResettingOnRelease,
                            onDragStopped = {
                                coroutineScope.launch {
                                    if (thresholdCrossed) {
                                        onSwipeToReply()
                                    }
                                    state.resetOffset()
                                }
                            },
                            state = state.draggableState,
                        )
                )
            }
        }
    } else {
        content(Modifier)
    }
}

/**
 * Impact ViewConfiguration.touchSlop by [sensitivityFactor].
 * Inspired from https://issuetracker.google.com/u/1/issues/269627294.
 * @param sensitivityFactor the factor to multiply the touchSlop by. The highest value, the more the user will
 * have to drag to start the drag.
 * @param content the content to display.
 */
@Composable
private fun SwipeSensitivity(
    sensitivityFactor: Float,
    content: @Composable () -> Unit,
) {
    val current = LocalViewConfiguration.current
    CompositionLocalProvider(
        LocalViewConfiguration provides object : ViewConfiguration by current {
            override val touchSlop: Float
                get() = current.touchSlop * sensitivityFactor
        }
    ) {
        content()
    }
}
