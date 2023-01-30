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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = ElementTheme.colors.primary,
        activeTrackColor = ElementTheme.colors.primary,
        activeTickColor = ElementTheme.colors.primary,
        inactiveTrackColor = ElementTheme.colors.primary,
        inactiveTickColor = ElementTheme.colors.primary,
        disabledThumbColor = ElementTheme.colors.primary.copy(alpha = 0.40f),
        disabledActiveTrackColor = ElementTheme.colors.primary.copy(alpha = 0.40f),
        disabledActiveTickColor = ElementTheme.colors.primary.copy(alpha = 0.40f),
        disabledInactiveTrackColor = ElementTheme.colors.primary.copy(alpha = 0.40f),
        disabledInactiveTickColor = ElementTheme.colors.primary.copy(alpha = 0.40f),
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Preview
@Composable
fun SlidersLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun SlidersDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        Slider(onValueChange = {}, value = 0.33f, enabled = true)
        Slider(onValueChange = {}, value = 0.33f, enabled = false)
    }
}
