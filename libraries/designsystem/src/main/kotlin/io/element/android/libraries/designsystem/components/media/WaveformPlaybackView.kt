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

package io.element.android.libraries.designsystem.components.media

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.compound.theme.ElementTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.roundToInt

private const val DEFAULT_GRAPHICS_LAYER_ALPHA: Float = 0.99F

/**
 * A view that displays a waveform and a cursor to indicate the current playback progress.
 *
 * @param playbackProgress The current playback progress, between 0 and 1.
 * @param showCursor Whether to show the cursor or not.
 * @param waveform The waveform to display. Use [FakeWaveformFactory] to generate a fake waveform.
 * @param onSeek Callback when the user seeks the waveform. Called with a value between 0 and 1.
 * @param modifier The modifier to be applied to the view.
 * @param seekEnabled Whether the user can seek the waveform or not.
 * @param brush The brush to use to draw the waveform.
 * @param progressBrush The brush to use to draw the progress.
 * @param cursorBrush The brush to use to draw the cursor.
 * @param lineWidth The width of the waveform lines.
 * @param linePadding The padding between waveform lines.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WaveformPlaybackView(
    playbackProgress: Float,
    showCursor: Boolean,
    waveform: ImmutableList<Float>,
    onSeek: (progress: Float) -> Unit,
    modifier: Modifier = Modifier,
    seekEnabled: Boolean = true,
    brush: Brush = SolidColor(ElementTheme.colors.iconQuaternary),
    progressBrush: Brush = SolidColor(ElementTheme.colors.iconSecondary),
    cursorBrush: Brush = SolidColor(ElementTheme.colors.iconAccentTertiary),
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
) {
    val seekProgress = remember { mutableStateOf<Float?>(null) }
    var canvasSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var canvasSizePx by remember { mutableStateOf(Size(0f, 0f)) }
    val progress by remember(playbackProgress, seekProgress.value) {
        derivedStateOf {
            seekProgress.value ?: playbackProgress
        }
    }
    val progressAnimated = animateFloatAsState(targetValue = progress, label = "progressAnimation")
    val amplitudeDisplayCount by remember(canvasSize, lineWidth, linePadding) {
        derivedStateOf {
            (canvasSize.width.value / (lineWidth.value + linePadding.value)).toInt()
        }
    }
    val normalizedWaveformData by remember(amplitudeDisplayCount) {
        derivedStateOf {
            waveform.normalisedData(amplitudeDisplayCount)
        }
    }

    val requestDisallowInterceptTouchEvent = remember { RequestDisallowInterceptTouchEvent() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = DEFAULT_GRAPHICS_LAYER_ALPHA)
            .let {
                if (!seekEnabled) return@let it

                it.pointerInteropFilter(requestDisallowInterceptTouchEvent = requestDisallowInterceptTouchEvent) { e ->
                    return@pointerInteropFilter when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (e.x in 0F..canvasSizePx.width) {
                                requestDisallowInterceptTouchEvent.invoke(true)
                                seekProgress.value = e.x / canvasSizePx.width
                                true
                            } else false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (e.x in 0F..canvasSizePx.width) {
                                seekProgress.value = e.x / canvasSizePx.width
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            requestDisallowInterceptTouchEvent.invoke(false)
                            seekProgress.value?.let(onSeek)
                            seekProgress.value = null
                            true
                        }
                        else -> false
                    }
                }
            }
            .then(modifier)
    ) {
        canvasSize = size.toDpSize()
        canvasSizePx = size
        val centerY = canvasSize.height.toPx() / 2
        val cornerRadius = lineWidth / 2
        drawWaveform(
            waveformData = normalizedWaveformData,
            canvasSize = canvasSize,
            brush = brush,
            lineWidth = lineWidth,
            linePadding = linePadding
        )
        drawRect(
            brush = progressBrush,
            size = Size(
                width = progressAnimated.value * canvasSize.width.toPx(),
                height = canvasSize.height.toPx()
            ),
            blendMode = BlendMode.SrcAtop
        )
        if (showCursor || seekProgress.value != null) {
            drawRoundRect(
                brush = cursorBrush,
                topLeft = Offset(
                    x = progressAnimated.value * canvasSize.width.toPx(),
                    y = centerY - (canvasSize.height.toPx() - 2) / 2
                ),
                size = Size(
                    width = lineWidth.toPx(),
                    height = canvasSize.height.toPx() - 2
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Fill
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun WaveformPlaybackViewPreview() = ElementPreview {
    Column {
        WaveformPlaybackView(
            modifier = Modifier.height(34.dp),
            showCursor = false,
            playbackProgress = 0.5f,
            onSeek = {},
            waveform = persistentListOf(),
        )
        WaveformPlaybackView(
            modifier = Modifier.height(34.dp),
            showCursor = false,
            playbackProgress = 0.5f,
            onSeek = {},
            waveform = persistentListOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f),
        )
        WaveformPlaybackView(
            modifier = Modifier.height(34.dp),
            showCursor = true,
            playbackProgress = 0.5f,
            onSeek = {},
            waveform = List(1024) { it / 1024f }.toPersistentList(),
        )
    }
}

private fun ImmutableList<Float>.normalisedData(maxSamplesCount: Int): ImmutableList<Float> {
    if (maxSamplesCount <= 0) {
        return persistentListOf()
    }

    // Filter the data to keep only the expected number of samples
    val result = if (this.size > maxSamplesCount) {
        (0..<maxSamplesCount)
            .map { index ->
                val targetIndex = (index.toDouble() * (this.count().toDouble() / maxSamplesCount.toDouble())).roundToInt()
                this[targetIndex]
            }
    } else {
        this
    }

    return result.toPersistentList()
}
