/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import io.element.android.compound.theme.ElementTheme

/**
 * A layout that measures its content to set the height offset limit of a [TopAppBarScrollBehavior].
 * It places the content according to the current height offset of the scroll behavior.
 *
 */
@ExperimentalMaterial3Api
@Composable
fun TopAppBarScrollBehaviorLayout(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElementTheme.colors.bgCanvasDefault,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable @UiComposable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Layout(
            content = content,
            measurePolicy = { measurables, constraints ->
                val placeable = measurables.first().measure(constraints)
                val contentHeight = placeable.height.toFloat()
                scrollBehavior.state.heightOffsetLimit = -contentHeight
                val heightOffset = scrollBehavior.state.heightOffset
                val layoutHeight = (contentHeight + heightOffset).toInt()
                layout(placeable.width, layoutHeight) {
                    placeable.place(0, heightOffset.toInt())
                }
            }
        )
    }
}
