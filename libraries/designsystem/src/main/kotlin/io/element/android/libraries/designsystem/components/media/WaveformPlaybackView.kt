/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
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
    brush: Brush = SolidColor(ElementTheme.colors.iconQuaternary),
    progressBrush: Brush = SolidColor(ElementTheme.colors.iconSecondary),
    cursorBrush: Brush = SolidColor(ElementTheme.colors.iconAccentTertiary),
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
) {
    val seekProgress = remember { mutableStateOf<Float?>(null) }
    // Last user-seek target, kept until the player reports a nearby sample.
    var pendingSeek by remember { mutableStateOf<Float?>(null) }
    var canvasSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var canvasSizePx by remember { mutableStateOf(Size(0f, 0f)) }
    val progressAnimated = remember { Animatable(playbackProgress) }
    // Bumped after a snap or user seek so AnimateCursorWhilePlaying restarts its tween.
    var seekGeneration by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    ApplyPlayerProgressToCursor(
        playbackProgress = playbackProgress,
        isPlaying = isPlaying,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
        pendingSeek = pendingSeek,
        seekProgress = seekProgress,
        progressAnimated = progressAnimated,
        onAcknowledgePendingSeek = { pendingSeek = null },
        onCursorSnap = { seekGeneration++ },
    )

    AnimateCursorWhilePlaying(
        isPlaying = isPlaying,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
        seekGeneration = seekGeneration,
        progressAnimated = progressAnimated,
    )

    val progress = seekProgress.value ?: progressAnimated.value
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
        derivedStateOf { with(density) { normalizedWaveformData.size * (lineWidth + linePadding).roundToPx().toFloat() } }
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
                            if (e.x in 0F..waveformWidthPx) {
                                requestDisallowInterceptTouchEvent.invoke(true)
                                seekProgress.value = e.x / waveformWidthPx
                                true
                            } else {
                                false
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (e.x in 0F..waveformWidthPx) {
                                seekProgress.value = e.x / waveformWidthPx
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            requestDisallowInterceptTouchEvent.invoke(false)
                            seekProgress.value?.let { seek ->
                                pendingSeek = seek
                                onSeek(seek)
                                coroutineScope.launch {
                                    progressAnimated.snapTo(seek)
                                    seekGeneration++
                                }
                            }
                            seekProgress.value = null
                            true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            requestDisallowInterceptTouchEvent.invoke(false)
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
                width = progress * waveformWidthPx,
                height = canvasSizePx.height
            ),
            blendMode = BlendMode.SrcAtop
        )
        if (showCursor || seekProgress.value != null) {
            drawRoundRect(
                brush = cursorBrush,
                topLeft = Offset(
                    x = progress * waveformWidthPx,
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

/**
 * Applies discrete player progress samples to the animated cursor.
 *
 * Samples that belong to a position before an in-flight user seek are ignored. Once a sample
 * is close enough to [pendingSeek], that seek is considered acknowledged. The cursor then
 * either snaps to the sample (pause, large jump, or playback restart) or keeps interpolating.
 *
 * @param playbackProgress Latest player progress, from 0 to 1.
 * @param isPlaying Whether media is currently playing.
 * @param durationMs Total media duration in milliseconds.
 * @param playbackSpeed Current playback rate (`1` = realtime).
 * @param pendingSeek Target progress of an in-flight user seek, or `null` if none.
 * @param seekProgress Progress while the user is dragging the cursor; non-null means a drag is in progress.
 * @param progressAnimated Animated cursor progress.
 * @param onAcknowledgePendingSeek Called when a player sample has caught up to [pendingSeek].
 * @param onCursorSnap Called after the cursor jumps, so the playing animation can restart from the new position.
 */
@Composable
private fun ApplyPlayerProgressToCursor(
    playbackProgress: Float,
    isPlaying: Boolean,
    durationMs: Long,
    playbackSpeed: Float,
    pendingSeek: Float?,
    seekProgress: State<Float?>,
    progressAnimated: Animatable<Float, AnimationVector1D>,
    onAcknowledgePendingSeek: () -> Unit,
    onCursorSnap: () -> Unit,
) {
    val latestOnAcknowledgePendingSeek by rememberUpdatedState(onAcknowledgePendingSeek)
    val latestOnCursorSnap by rememberUpdatedState(onCursorSnap)
    LaunchedEffect(playbackProgress, isPlaying, durationMs, playbackSpeed, pendingSeek) {
        if (seekProgress.value != null) return@LaunchedEffect
        if (!shouldApplyPlayerProgress(
                playbackProgress = playbackProgress,
                pendingSeek = pendingSeek,
                durationMs = durationMs,
                playbackSpeed = playbackSpeed,
            )
        ) {
            return@LaunchedEffect
        }
        latestOnAcknowledgePendingSeek()
        if (shouldSnapToPlayer(
                playbackProgress = playbackProgress,
                animatedProgress = progressAnimated.value,
                isPlaying = isPlaying,
                durationMs = durationMs,
                playbackSpeed = playbackSpeed,
            )
        ) {
            progressAnimated.snapTo(playbackProgress)
            latestOnCursorSnap()
        }
    }
}

/**
 * Animates the cursor linearly from its current position to the end of the media.
 *
 * Player samples are relatively infrequent, so between them we interpolate using the remaining
 * duration and [playbackSpeed]. Changing [seekGeneration] (after a snap or user seek) cancels
 * the current tween and starts a new one from the updated cursor position.
 *
 * @param isPlaying Whether media is currently playing. Animation only runs while this is true.
 * @param durationMs Total media duration in milliseconds. Must be `> 0` to animate.
 * @param playbackSpeed Current playback rate (`1` = realtime). Faster speeds shorten the tween.
 * @param seekGeneration Counter incremented when the cursor jumps, used to restart this effect.
 * @param progressAnimated Animated cursor progress, tweened toward `1`.
 */
@Composable
private fun AnimateCursorWhilePlaying(
    isPlaying: Boolean,
    durationMs: Long,
    playbackSpeed: Float,
    seekGeneration: Int,
    progressAnimated: Animatable<Float, AnimationVector1D>,
) {
    LaunchedEffect(isPlaying, durationMs, playbackSpeed, seekGeneration) {
        if (isPlaying && durationMs > 0L) {
            val remainingProgress = (1f - progressAnimated.value).coerceAtLeast(0f)
            val remainingMs = (remainingProgress * durationMs / playbackSpeed.coerceAtLeast(0.01f))
                .roundToInt()
                .coerceAtLeast(0)
            if (remainingMs == 0) {
                progressAnimated.snapTo(1f)
            } else {
                progressAnimated.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = remainingMs, easing = LinearEasing),
                )
            }
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
