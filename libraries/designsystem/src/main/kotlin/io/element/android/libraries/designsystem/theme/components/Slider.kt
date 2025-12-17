/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    // @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinish: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    useCustomLayout: Boolean = false,
) {
    val thumbColor = ElementTheme.colors.iconOnSolidPrimary
    var isUserInteracting by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            isUserInteracting = when (interaction) {
                is DragInteraction.Start,
                is PressInteraction.Press -> true
                else -> false
            }
        }
    }
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinish,
        colors = colors,
        thumb = {
            if (useCustomLayout) {
                SliderDefaults.Thumb(
                    modifier = Modifier.drawWithContent {
                        drawContent()
                        if (isUserInteracting.not()) {
                            drawCircle(thumbColor, radius = 8.dp.toPx())
                        }
                    },
                    interactionSource = interactionSource,
                    colors = colors.copy(
                        thumbColor = ElementTheme.colors.iconPrimary,
                    ),
                    enabled = enabled,
                    thumbSize = DpSize(
                        if (isUserInteracting) 44.dp else 22.dp,
                        22.dp,
                    ),
                )
            } else {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    enabled = enabled
                )
            }
        },
        track = { sliderState ->
            if (useCustomLayout) {
                SliderDefaults.Track(
                    modifier = Modifier.height(8.dp),
                    colors = colors.copy(
                        activeTrackColor = Color(0x66E0EDFF),
                        inactiveTrackColor = Color(0x66E0EDFF),
                    ),
                    enabled = enabled,
                    sliderState = sliderState,
                    thumbTrackGapSize = 0.dp,
                    drawStopIndicator = { },
                )
            } else {
                SliderDefaults.Track(
                    colors = colors,
                    enabled = enabled,
                    sliderState = sliderState,
                )
            }
        },
        interactionSource = interactionSource,
    )
}

@Preview(group = PreviewGroup.Sliders)
@Composable
internal fun SlidersPreview() = ElementThemedPreview {
    var value by remember { mutableFloatStateOf(0.33f) }
    Column {
        Slider(onValueChange = { value = it }, value = value, enabled = true)
        Slider(steps = 10, onValueChange = { value = it }, value = value, enabled = true)
        Slider(onValueChange = { value = it }, value = value, enabled = false)
        Slider(onValueChange = { value = it }, value = value, enabled = true, useCustomLayout = true)
    }
}
