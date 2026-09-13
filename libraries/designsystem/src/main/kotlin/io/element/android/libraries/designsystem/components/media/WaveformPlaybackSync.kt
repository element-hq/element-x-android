/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import kotlin.math.abs

internal const val PLAYING_SYNC_THRESHOLD_MS = 200L
private const val ZERO_DURATION_PROGRESS_EPSILON = 0.01f

/**
 * Whether a player progress sample should be applied, or ignored because a seek has not been
 * acknowledged yet.
 */
internal fun shouldApplyPlayerProgress(
    playbackProgress: Float,
    pendingSeek: Float?,
    durationMs: Long,
    playbackSpeed: Float,
    syncThresholdMs: Long = PLAYING_SYNC_THRESHOLD_MS,
): Boolean {
    if (pendingSeek == null) return true
    return hasCaughtUpToSeek(
        playbackProgress = playbackProgress,
        pendingSeek = pendingSeek,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
        syncThresholdMs = syncThresholdMs,
    )
}

internal fun hasCaughtUpToSeek(
    playbackProgress: Float,
    pendingSeek: Float,
    durationMs: Long,
    playbackSpeed: Float,
    syncThresholdMs: Long = PLAYING_SYNC_THRESHOLD_MS,
): Boolean {
    val delta = abs(playbackProgress - pendingSeek)
    if (durationMs <= 0L) {
        return delta <= ZERO_DURATION_PROGRESS_EPSILON
    }
    val deltaMs = delta * durationMs / playbackSpeed.coerceAtLeast(0.01f)
    return deltaMs <= syncThresholdMs
}

private const val RESTART_PROGRESS_EPSILON = 0.01f
private const val RESTART_ANIMATED_PROGRESS = 0.9f

/**
 * Whether the animated cursor should snap to the latest player sample.
 */
internal fun shouldSnapToPlayer(
    playbackProgress: Float,
    animatedProgress: Float,
    isPlaying: Boolean,
    durationMs: Long,
    playbackSpeed: Float,
    syncThresholdMs: Long = PLAYING_SYNC_THRESHOLD_MS,
): Boolean {
    val delta = abs(playbackProgress - animatedProgress)
    if (delta == 0f) return false
    if (!isPlaying || durationMs <= 0L) return true
    val deltaMs = delta * durationMs / playbackSpeed.coerceAtLeast(0.01f)
    if (playbackProgress < animatedProgress) {
        return isPlaybackRestart(playbackProgress, animatedProgress)
    }
    return deltaMs > syncThresholdMs
}

private fun isPlaybackRestart(
    playbackProgress: Float,
    animatedProgress: Float,
): Boolean {
    return playbackProgress <= RESTART_PROGRESS_EPSILON && animatedProgress >= RESTART_ANIMATED_PROGRESS
}
