/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.ruler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Horizontal ruler is a debug composable that displays a horizontal ruler.
 * It can be used to display the horizontal ruler in the composable preview.
 */
@Composable
fun HorizontalRuler(
    modifier: Modifier = Modifier,
) {
    val baseColor = Color.Magenta
    val alphaBaseColor = baseColor.copy(alpha = 0.2f)
    Row(modifier = modifier.fillMaxWidth()) {
        repeat(50) {
            HorizontalRulerItem(1.dp, alphaBaseColor)
            HorizontalRulerItem(2.dp, baseColor)
            HorizontalRulerItem(1.dp, alphaBaseColor)
            HorizontalRulerItem(2.dp, baseColor)
            HorizontalRulerItem(5.dp, alphaBaseColor)
            HorizontalRulerItem(2.dp, baseColor)
            HorizontalRulerItem(1.dp, alphaBaseColor)
            HorizontalRulerItem(2.dp, baseColor)
            HorizontalRulerItem(1.dp, alphaBaseColor)
            HorizontalRulerItem(10.dp, baseColor)
        }
    }
}

@Composable
private fun HorizontalRulerItem(height: Dp, color: Color) {
    Spacer(
        modifier = Modifier
            .size(height = height, width = 1.dp)
            .background(color = color)
    )
}

@PreviewsDayNight
@Composable
internal fun HorizontalRulerPreview() = ElementPreview {
    HorizontalRuler()
}
