/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collectLatest

/**
 * Remembers a [SmoothPlaybackProgressState], fed with the player progress samples.
 *
 * @param playbackProgress Latest player progress, from 0 to 1.
 * @param isPlaying Whether media is currently playing. Progress is only extrapolated while this is true.
 * @param durationMs Total media duration in milliseconds. Must be `> 0` to extrapolate.
 * @param playbackSpeed Current playback rate (`1` = realtime).
 */
@Composable
internal fun rememberSmoothPlaybackProgressState(
    playbackProgress: Float,
    isPlaying: Boolean,
    durationMs: Long,
    playbackSpeed: Float,
): SmoothPlaybackProgressState {
    val state = remember { SmoothPlaybackProgressState(playbackProgress) }
    // Applied before the next draw, so a new sample is never shown late.
    SideEffect { state.onPlayerProgress(playbackProgress) }
    LaunchedEffect(state, isPlaying, durationMs, playbackSpeed) {
        state.animate(isPlaying = isPlaying, durationMs = durationMs, playbackSpeed = playbackSpeed)
    }
    return state
}

/**
 * Playback progress that moves smoothly while playing.
 *
 * Player samples are relatively infrequent, so on every frame the progress is extrapolated from a start point, using the time
 * elapsed since then and the playback speed. Each new player sample or user seek becomes the new start point, so the progress
 * cannot drift away from the player, and a seek is shown immediately without waiting for the player to report it.
 *
 * If the system disables animations, the progress only follows the player samples and seeks, and the extrapolation restarts when
 * they are enabled again. Other animator duration scales are ignored, since the cursor tracks the audio.
 */
@Stable
internal class SmoothPlaybackProgressState(initialProgress: Float) {
    /** The progress to display, from 0 to 1. */
    var progress by mutableFloatStateOf(initialProgress)
        private set

    private var lastPlayerProgress = initialProgress

    // Progress the extrapolation starts from, and its frame time, or null to start at the next frame.
    private var startProgress = initialProgress
    private var startTimeMs: Long? = null

    /** Restarts from a new player sample. A sample equal to the previous one is ignored, so it does not undo a seek. */
    fun onPlayerProgress(playbackProgress: Float) {
        if (playbackProgress == lastPlayerProgress) return
        lastPlayerProgress = playbackProgress
        restartFrom(playbackProgress)
    }

    /** Restarts from a user seek, until the player reports its new position. */
    fun seekTo(seekProgress: Float) {
        restartFrom(seekProgress)
    }

    /** Extrapolates [progress] on every frame while [isPlaying] and animations are enabled. */
    suspend fun animate(isPlaying: Boolean, durationMs: Long, playbackSpeed: Float) {
        // Restart from the displayed progress, so that time spent paused, or at a previous speed, is not counted.
        restartFrom(progress)
        if (!isPlaying || durationMs <= 0L) return
        val motionDurationScale = currentCoroutineContext()[MotionDurationScale]
        snapshotFlow { motionDurationScale?.scaleFactor != 0f }.collectLatest { animationsEnabled ->
            if (animationsEnabled) {
                restartFrom(progress)
                extrapolateOnEachFrame(durationMs = durationMs, playbackSpeed = playbackSpeed)
            } else {
                // Display the latest player sample or seek.
                restartFrom(startProgress)
            }
        }
    }

    private suspend fun extrapolateOnEachFrame(durationMs: Long, playbackSpeed: Float): Nothing {
        while (true) {
            withFrameMillis { frameTimeMs ->
                val startTimeMs = startTimeMs ?: frameTimeMs.also { startTimeMs = it }
                progress = extrapolatePlaybackProgress(
                    progress = startProgress,
                    elapsedMs = frameTimeMs - startTimeMs,
                    durationMs = durationMs,
                    playbackSpeed = playbackSpeed,
                )
            }
        }
    }

    private fun restartFrom(startProgress: Float) {
        this.startProgress = startProgress
        startTimeMs = null
        progress = startProgress
    }
}

/**
 * Estimated playback progress [elapsedMs] after the player reported [progress], assuming playback continued at [playbackSpeed].
 *
 * @param progress Latest player progress, from 0 to 1.
 * @param elapsedMs Wall-clock milliseconds since [progress] was received.
 * @param durationMs Total media duration in milliseconds. If `<= 0`, [progress] is returned unchanged.
 * @param playbackSpeed Current playback rate (`1` = realtime).
 * @return The estimated progress, never past `1`.
 */
internal fun extrapolatePlaybackProgress(
    progress: Float,
    elapsedMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
): Float {
    if (durationMs <= 0L || !playbackSpeed.isFinite()) return progress
    val elapsedProgress = elapsedMs.coerceAtLeast(0L) * playbackSpeed.coerceAtLeast(0f) / durationMs
    return (progress + elapsedProgress).coerceAtMost(1f)
}
