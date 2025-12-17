/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Light gradient background for Join room screens.
 */
@Composable
fun LightGradientBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElementTheme.colors.bgCanvasDefault,
    firstColor: Color = Color(0x1E0DBD8B),
    secondColor: Color = Color(0x001273EB),
    ratio: Float = 642 / 775f,
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val biggerDimension = size.width * 1.98f
        val gradientShaderBrush = ShaderBrush(
            RadialGradientShader(
                colors = listOf(firstColor, secondColor),
                center = size.center.copy(x = size.width * ratio, y = size.height * ratio),
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        )
        drawRect(backgroundColor, size = size)
        drawRect(brush = gradientShaderBrush, size = size)
    }
}

@PreviewsDayNight
@Composable
internal fun LightGradientBackgroundPreview() = ElementPreview {
    LightGradientBackground()
}
