/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.ruler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Vertical ruler is a debug composable that displays a vertical ruler.
 * It can be used to display the vertical ruler in the composable preview.
 */
@Composable
fun VerticalRuler(
    modifier: Modifier = Modifier,
) {
    val baseColor = Color.Red
    val alphaBaseColor = baseColor.copy(alpha = 0.2f)
    Column(modifier = modifier.fillMaxHeight()) {
        repeat(50) {
            VerticalRulerItem(1.dp, alphaBaseColor)
            VerticalRulerItem(2.dp, baseColor)
            VerticalRulerItem(1.dp, alphaBaseColor)
            VerticalRulerItem(2.dp, baseColor)
            VerticalRulerItem(5.dp, alphaBaseColor)
            VerticalRulerItem(2.dp, baseColor)
            VerticalRulerItem(1.dp, alphaBaseColor)
            VerticalRulerItem(2.dp, baseColor)
            VerticalRulerItem(1.dp, alphaBaseColor)
            VerticalRulerItem(10.dp, baseColor)
        }
    }
}

@Composable
private fun VerticalRulerItem(width: Dp, color: Color) {
    Spacer(
        modifier = Modifier
            .size(height = 1.dp, width = width)
            .background(color = color)
    )
}

@PreviewsDayNight
@Composable
internal fun VerticalRulerPreview() = ElementPreview {
    VerticalRuler()
}
