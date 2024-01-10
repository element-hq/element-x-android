/*
 * Copyright (c) 2023 New Vector Ltd
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
