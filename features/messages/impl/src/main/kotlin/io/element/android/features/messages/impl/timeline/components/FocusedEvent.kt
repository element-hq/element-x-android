/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.gradientSubtleColors
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toPx

/**
 * Background highlighting the focused event in the timeline.
 */
@Suppress("ModifierComposable")
@Composable
internal fun Modifier.focusedEvent(
    focusedEventOffset: Dp,
): Modifier {
    val highlightedLineColor = ElementTheme.colors.borderAccentSubtle
    val gradientColors = gradientSubtleColors()
    val verticalOffset = focusedEventOffset.toPx()
    val verticalRatio = 0.7f
    return drawWithCache {
        val brush = Brush.verticalGradient(
            colors = gradientColors,
            endY = size.height * verticalRatio,
        )
        onDrawBehind {
            drawRect(
                brush,
                topLeft = Offset(0f, verticalOffset),
                size = Size(size.width, size.height * verticalRatio)
            )
            drawLine(
                highlightedLineColor,
                start = Offset(0f, verticalOffset),
                end = Offset(size.width, verticalOffset)
            )
        }
    }.padding(top = 4.dp)
}

@PreviewsDayNight
@Composable
internal fun FocusedEventPreview() = ElementPreview {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .focusedEvent(0.dp),
    )
}
