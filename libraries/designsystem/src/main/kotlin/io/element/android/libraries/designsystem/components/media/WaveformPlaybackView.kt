/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

private const val DEFAULT_GRAPHICS_LAYER_ALPHA: Float = 0.99F

/**
 * A view that displays a waveform and a cursor to indicate the current playback progress.
 *
 * @param playbackProgress The current playback progress, between 0 and 1.
 * @param showCursor Whether to show the cursor or not.
 * @param waveform The waveform to display.
 * @param onSeek Callback when the user seeks the waveform. Called with a value between 0 and 1.
 * @param modifier The modifier to be applied to the view.
 * @param isPlaying Whether playback is currently in progress. Used to drive a linear progress animation.
 * @param durationMs The total duration of the media in milliseconds. Used to time the linear animation.
 * @param playbackSpeed The current playback speed. Used to time the linear animation.
 * @param seekEnabled Whether the user can seek the waveform or not.
 * @param onSeekProgressChange Called with the progress, between 0 and 1, while the user drags the cursor, and with `null` when the drag ends.
 * @param brush The brush to use to draw the waveform.
 * @param progressBrush The brush to use to draw the progress.
 * @param cursorBrush The brush to use to draw the cursor.
 * @param lineWidth The width of the waveform lines.
 * @param linePadding The padding between waveform lines.
 */
@Composable
fun WaveformPlaybackView(
    playbackProgress: Float,
    showCursor: Boolean,
    waveform: ImmutableList<Float>,
    onSeek: (progress: Float) -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    durationMs: Long = 0L,
    playbackSpeed: Float = 1f,
    seekEnabled: Boolean = true,
    onSeekProgressChange: (progress: Float?) -> Unit = {},
    brush: Brush = SolidColor(ElementTheme.colors.iconQuaternary),
    progressBrush: Brush = SolidColor(ElementTheme.colors.iconSecondary),
    cursorBrush: Brush = SolidColor(ElementTheme.colors.iconAccentTertiary),
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
) {
    val seekProgress = remember { mutableStateOf<Float?>(null) }
    var canvasSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var canvasSizePx by remember { mutableStateOf(Size(0f, 0f)) }
    val smoothProgress = rememberSmoothPlaybackProgressState(
        playbackProgress = playbackProgress,
        isPlaying = isPlaying,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
    )
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

    val density = LocalDensity.current
    val waveformWidthPx by remember {
        derivedStateOf { with(density) { normalizedWaveformData.size * (lineWidth + linePadding).toPx() } }
    }

    val requestDisallowInterceptTouchEvent = remember { RequestDisallowInterceptTouchEvent() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = DEFAULT_GRAPHICS_LAYER_ALPHA)
            .let {
                if (!seekEnabled) return@let it
                it.pointerInteropFilter(requestDisallowInterceptTouchEvent = requestDisallowInterceptTouchEvent) { e ->
                    handleWaveformPointerEvent(
                        action = e.action,
                        x = e.x,
                        waveformWidthPx = waveformWidthPx,
                        lineWidthPx = with(density) { lineWidth.toPx() },
                        linePaddingPx = with(density) { linePadding.toPx() },
                        currentSeekProgress = seekProgress.value,
                        onDisallowParentIntercept = { requestDisallowInterceptTouchEvent.invoke(it) },
                        onUpdateSeekProgress = {
                            seekProgress.value = it
                            onSeekProgressChange(it)
                        },
                        onCommitSeek = { seek ->
                            smoothProgress.seekTo(seek)
                            onSeek(seek)
                        },
                    )
                }
            }
            .then(modifier)
            .onSizeChanged {
                canvasSizePx = it.toSize()
                canvasSize = with(density) { canvasSizePx.toDpSize() }
            }
    ) {
        // Read in the draw phase, so that the smooth progress only redraws the canvas on each frame.
        val progress = seekProgress.value ?: smoothProgress.progress
        val cornerRadius = lineWidth / 2
        // Calculate the size of the waveform by summing the width of all the lines and paddings
        drawWaveform(
            waveformData = normalizedWaveformData,
            canvasSizePx = canvasSizePx,
            brush = brush,
            lineWidth = lineWidth,
            linePadding = linePadding
        )
        drawRect(
            brush = progressBrush,
            size = Size(
                width = progress.coerceIn(0f, 1f) * waveformWidthPx,
                height = canvasSizePx.height
            ),
            blendMode = BlendMode.SrcAtop
        )
        if (showCursor || seekProgress.value != null) {
            drawRoundRect(
                brush = cursorBrush,
                topLeft = Offset(
                    x = cursorOffsetPx(
                        progress = progress,
                        waveformWidthPx = waveformWidthPx,
                        lineWidthPx = lineWidth.toPx(),
                        linePaddingPx = linePadding.toPx(),
                    ),
                    y = 1f
                ),
                size = Size(
                    width = lineWidth.toPx(),
                    height = canvasSizePx.height - 2
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
            waveform = WaveFormSamples.realisticWaveForm,
        )
        WaveformPlaybackView(
            modifier = Modifier.height(34.dp),
            showCursor = true,
            playbackProgress = 0.5f,
            onSeek = {},
            waveform = WaveFormSamples.allRangeWaveForm,
        )
    }
}

internal fun ImmutableList<Float>.normalisedData(maxSamplesCount: Int): ImmutableList<Float> {
    if (maxSamplesCount <= 0) {
        return persistentListOf()
    }

    if (isEmpty()) {
        return List(maxSamplesCount) { 0f }.toImmutableList()
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

    return result.toImmutableList()
}
