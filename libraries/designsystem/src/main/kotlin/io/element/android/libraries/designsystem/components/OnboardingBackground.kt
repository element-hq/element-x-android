/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.Canvas
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

/**
 * Gradient background for FTUE (onboarding) screens.
 */
@Suppress("ModifierMissing")
@Composable
fun OnboardingBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
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
