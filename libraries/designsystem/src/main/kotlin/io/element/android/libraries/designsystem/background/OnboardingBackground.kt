/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.drawWithLayer

/**
 * Gradient background for FTUE (onboarding) screens.
 */
@Suppress("ModifierMissing")
@Composable
fun OnboardingBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault)
    ) {
        val isLightTheme = ElementTheme.isLightTheme
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
        ) {
            val gradientBrush = ShaderBrush(
                LinearGradientShader(
                    from = Offset(0f, size.height / 2f),
                    to = Offset(size.width, size.height / 2f),
                    colors = listOf(
                        Color(0xFF0DBDA8),
                        if (isLightTheme) Color(0xC90D5CBD) else Color(0xFF0D5CBD),
                    )
                )
            )
            val eraseBrush = ShaderBrush(
                LinearGradientShader(
                    from = Offset(size.width / 2f, 0f),
                    to = Offset(size.width / 2f, size.height * 2f),
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0x00000000),
                    )
                )
            )
            drawWithLayer {
                drawRect(brush = gradientBrush, size = size)
                drawRect(brush = gradientBrush, size = size, blendMode = BlendMode.Overlay)
                drawRect(brush = eraseBrush, size = size, blendMode = BlendMode.DstOut)
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun OnboardingBackgroundPreview() {
    ElementPreview {
        OnboardingBackground()
    }
}
