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

package io.element.android.libraries.designsystem.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Light gradient background for Join room screens.
 */
@Composable
fun LightGradientBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
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
