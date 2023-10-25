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

package io.element.android.features.messages.impl.voicemessages.timeline

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.element.android.libraries.theme.ElementTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.max
import kotlin.math.roundToInt

data class Waveform (
    val data: ImmutableList<Int>
) {
    companion object {
        private val dataRange = 0..1024
    }

    fun normalisedData(maxSamplesCount: Int): ImmutableList<Float> {
        if(maxSamplesCount <= 0) {
            return persistentListOf()
        }

        // Filter the data to keep only the expected number of samples
        val result = if (data.size > maxSamplesCount) {
            (0..<maxSamplesCount)
                .map { index ->
                    val targetIndex = (index.toDouble() * (data.count().toDouble() / maxSamplesCount.toDouble())).roundToInt()
                    data[targetIndex]
                }
        } else {
            data
        }

        // Normalize the sample in the allowed range
        return result.map { it.toFloat() / dataRange.last.toFloat() }.toPersistentList()
    }
}
private const val DEFAULT_GRAPHICS_LAYER_ALPHA: Float = 0.99F
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WaveformPlaybackView(
    playbackProgress: Float,
    showCursor: Boolean,
    waveform: Waveform,
    modifier: Modifier = Modifier,
    onSeek: (progress: Float) -> Unit = {},
    brush: Brush = SolidColor(ElementTheme.colors.iconQuaternary),
    progressBrush: Brush  = SolidColor(ElementTheme.colors.iconSecondary),
    cursorBrush: Brush  = SolidColor(ElementTheme.colors.iconAccentTertiary),
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
    minimumGraphAmplitude: Float = 2F,
) {
    var seekProgress = remember { mutableStateOf<Float?>(null) }
    var canvasSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var canvasSizePx by remember { mutableStateOf(Size(0f, 0f)) }
    val progress by remember(playbackProgress, seekProgress.value) {
        derivedStateOf {
            seekProgress.value ?: playbackProgress
        }
    }
    val progressAnimated = animateFloatAsState(targetValue = progress, label = "progressAnimation")
    val amplitudeDisplayCount by remember(canvasSize) {
        derivedStateOf {
            ((canvasSize.width.value) / (lineWidth.value + linePadding.value)).toInt()
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
            .pointerInteropFilter(requestDisallowInterceptTouchEvent = requestDisallowInterceptTouchEvent) {
                return@pointerInteropFilter when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (it.x in 0F..canvasSizePx.width) {
                            requestDisallowInterceptTouchEvent.invoke(true)
                            seekProgress.value = (it.x / canvasSizePx.width)
                            true
                        } else false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (it.x in 0F..canvasSizePx.width) {
                            seekProgress.value = (it.x / canvasSizePx.width)
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
            .then(modifier)
    ) {
        canvasSize = size.toDpSize()
        canvasSizePx = size
        val centerY = canvasSize.height.toPx() / 2
        val cornerRadius = lineWidth / 2
        normalizedWaveformData.forEachIndexed { index, amplitude ->
            val drawingAmplitude = max(minimumGraphAmplitude, amplitude * (canvasSize.height.toPx() - 2))
            drawRoundRect(
                brush = brush,
                topLeft = Offset(
                    x = index * (linePadding + lineWidth).toPx(),
                    y = centerY - (drawingAmplitude / 2)
                ),
                size = Size(
                    width = lineWidth.toPx(),
                    height = drawingAmplitude
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Fill
            )
        }
        drawRect(
            brush = progressBrush,
            size = Size(
                width = (progressAnimated.value) * canvasSize.width.toPx(),
                height = canvasSize.height.toPx()
            ),
            blendMode = BlendMode.SrcAtop
        )
        if(showCursor || seekProgress.value != null) {
            drawRoundRect(
                brush = cursorBrush,
                topLeft = Offset(
                    x = progressAnimated.value * canvasSize.width.toPx(),
                    y = centerY - ((canvasSize.height.toPx() - 2) / 2)
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
            showCursor = false,
            playbackProgress = 0.5f,
            waveform =  Waveform(persistentListOf()),
        )
        WaveformPlaybackView(
            showCursor = false,
            playbackProgress = 0.5f,
            waveform =  Waveform(persistentListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)),
        )
        WaveformPlaybackView(
            showCursor = true,
            playbackProgress = 0.5f,
            waveform =  Waveform(List(1024) { it }.toPersistentList()),
        )
    }
}
