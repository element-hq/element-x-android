/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

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
