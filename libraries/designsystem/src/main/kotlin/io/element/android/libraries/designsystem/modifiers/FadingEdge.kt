/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun horizontalFadingEdgesBrush(
    showLeft: Boolean,
    showRight: Boolean,
    percent: Float = 0.1f,
): Brush {
    val leftColor by animateColorAsState(
        targetValue = if (showLeft) Color.Transparent else Color.White,
        label = "AnimateLeftColor",
    )
    val rightColor by animateColorAsState(
        targetValue = if (showRight) Color.Transparent else Color.White,
        label = "AnimateRightColor",
    )
    return Brush.horizontalGradient(
        0f to leftColor,
        percent to Color.White,
        1f - percent to Color.White,
        1f to rightColor
    )
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
