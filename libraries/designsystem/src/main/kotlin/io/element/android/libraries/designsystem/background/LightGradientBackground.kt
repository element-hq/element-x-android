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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

class LightGradientBackground(
    private val firstColor: Color = Color(0x1E0DBD8B),
    private val secondColor: Color = Color(0x001273EB),
    private val ratio: Float = 642 / 775f,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val biggerDimension = size.width * 1.98f
        return RadialGradientShader(
            colors = listOf(firstColor, secondColor),
            center = size.center.copy(x = size.width * ratio, y = size.height * ratio),
            radius = biggerDimension / 2f,
            colorStops = listOf(0f, 0.95f)
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LightGradientBackgroundPreview() = ElementPreview {
    val gradientBackground = remember {
        LightGradientBackground()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    )
}
