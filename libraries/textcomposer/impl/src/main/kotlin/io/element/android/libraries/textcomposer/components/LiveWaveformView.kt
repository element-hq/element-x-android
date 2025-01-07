/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.media.drawWaveform
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.lang.Float.min

private const val DEFAULT_GRAPHICS_LAYER_ALPHA: Float = 0.99F
private val waveFormHeight = 26.dp

@Composable
fun LiveWaveformView(
    levels: ImmutableList<Float>,
    modifier: Modifier = Modifier,
    brush: Brush = SolidColor(ElementTheme.colors.iconQuaternary),
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
) {
    var canvasSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }

    var parentWidth by remember { mutableIntStateOf(0) }

    val waveformWidth by remember(levels, lineWidth, linePadding) {
        derivedStateOf {
            levels.size * (lineWidth.value + linePadding.value)
        }
    }

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .fillMaxWidth()
            .height(waveFormHeight)
            .onSizeChanged { parentWidth = it.width }
    ) {
        Canvas(
            modifier = Modifier
                .width(Dp(waveformWidth))
                .graphicsLayer(alpha = DEFAULT_GRAPHICS_LAYER_ALPHA)
                .then(modifier)
        ) {
            val width = min(waveformWidth, parentWidth.toFloat())
            canvasSize = DpSize(width.dp, size.height.toDp())
            val countThatFitsWidth = (parentWidth.toFloat() / (lineWidth.toPx() + linePadding.toPx())).toInt()
            drawWaveform(
                waveformData = levels.takeLast(countThatFitsWidth).toPersistentList(),
                canvasSizePx = Size(canvasSize.width.toPx(), size.height),
                brush = brush,
                lineWidth = lineWidth,
                linePadding = linePadding,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LiveWaveformViewPreview() = ElementPreview {
    Column {
        LiveWaveformView(
            levels = List(100) { it.toFloat() / 100 }.toPersistentList(),
            modifier = Modifier.height(34.dp),
        )
        LiveWaveformView(
            levels = List(40) { it.toFloat() / 40 }.toPersistentList(),
            modifier = Modifier.height(34.dp),
        )
    }
}
