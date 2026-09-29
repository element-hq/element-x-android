/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Maximum gap, in wall-clock milliseconds, between two progress values that we still treat as
 * "the same place" while audio is playing.
 *
 * Player progress is sampled on a timer, while the waveform cursor is animated every frame.
 * A gap smaller than this is expected sampling lag and should be ignored. A larger gap means a
 * real jump (seek acknowledged, pause, or the animation has drifted) and the cursor should snap.
 *
 * The comparison uses wall-clock time, so faster playback makes the same progress-fraction
 * delta count as a smaller time gap.
 */
internal const val PLAYING_SYNC_THRESHOLD_MS = 200L

/**
 * When media duration is unknown (`durationMs <= 0`), progress is compared as a 0–1 fraction
 * instead of milliseconds. Values this close are treated as equal.
 */
private const val ZERO_DURATION_PROGRESS_EPSILON = 0.01f

/**
 * Whether a player progress sample should update the cursor, or be ignored because a user seek
 * has not been acknowledged yet.
 *
 * After a seek, the player can keep emitting samples from the old position. Those are dropped
 * until a sample is close enough to [pendingSeek].
 *
 * @param playbackProgress Latest player progress, from 0 to 1.
 * @param pendingSeek Target progress of an in-flight user seek, or `null` if none.
 * @param durationMs Total media duration in milliseconds. Used to convert a progress delta into time.
 * @param playbackSpeed Current playback rate (`1` = realtime). Used when converting the delta to wall-clock time.
 * @param syncThresholdMs How close the sample must be to [pendingSeek], in milliseconds. See [PLAYING_SYNC_THRESHOLD_MS].
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

/**
 * Whether [playbackProgress] is close enough to [pendingSeek] that the player has likely applied
 * the seek.
 *
 * @param playbackProgress Latest player progress, from 0 to 1.
 * @param pendingSeek Target progress of the in-flight user seek, from 0 to 1.
 * @param durationMs Total media duration in milliseconds. If `<= 0`, [ZERO_DURATION_PROGRESS_EPSILON] is used instead.
 * @param playbackSpeed Current playback rate (`1` = realtime).
 * @param syncThresholdMs Allowed wall-clock gap in milliseconds. See [PLAYING_SYNC_THRESHOLD_MS].
 */
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

/** Player progress at or below this is treated as "at the start" when detecting a loop/restart. */
private const val RESTART_PROGRESS_EPSILON = 0.01f

/** Animated cursor at or above this is treated as "at the end" when detecting a loop/restart. */
private const val RESTART_ANIMATED_PROGRESS = 0.9f

/**
 * Whether the animated cursor should jump to the latest player sample instead of continuing its
 * linear animation.
 *
 * Returns `true` when:
 * - playback is paused or duration is unknown (there is no animation to follow)
 * - the player is ahead of the cursor by more than [syncThresholdMs]
 * - playback restarted from the beginning (cursor near the end, player near the start)
 *
 * Returns `false` when the values already match, or when the player is only slightly ahead
 * (or slightly behind) so the in-flight animation can catch up without a visible jump.
 *
 * @param playbackProgress Latest player progress, from 0 to 1.
 * @param animatedProgress Current animated cursor progress, from 0 to 1.
 * @param isPlaying Whether media is currently playing.
 * @param durationMs Total media duration in milliseconds. Used to convert a progress delta into time.
 * @param playbackSpeed Current playback rate (`1` = realtime). Used when converting the delta to wall-clock time.
 * @param syncThresholdMs Drift that is still ignored while playing, in milliseconds. See [PLAYING_SYNC_THRESHOLD_MS].
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

/**
 * Detects a loop: the player has wrapped to the start while the cursor is still near the end.
 */
private fun isPlaybackRestart(
    playbackProgress: Float,
    animatedProgress: Float,
): Boolean {
    return playbackProgress <= RESTART_PROGRESS_EPSILON && animatedProgress >= RESTART_ANIMATED_PROGRESS
}

/**
 * Remaining wall-clock milliseconds to tween the cursor from [progress] to the end of the media.
 *
 * Used to time the linear playback animation between infrequent player samples. A result of `0`
 * means the cursor is already at (or past) the end and should snap rather than tween.
 *
 * @param progress Current cursor progress, from 0 to 1.
 * @param durationMs Total media duration in milliseconds.
 * @param playbackSpeed Current playback rate (`1` = realtime). Faster speeds shorten the result.
 */
internal fun remainingPlaybackAnimationMs(
    progress: Float,
    durationMs: Long,
    playbackSpeed: Float,
): Int {
    val remainingProgress = (1f - progress).coerceAtLeast(0f)
    return (remainingProgress * durationMs / playbackSpeed.coerceAtLeast(0.01f))
        .roundToInt()
        .coerceAtLeast(0)
}
