/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WaveformPlaybackSyncTest {
    @Test
    fun `paused cursor always snaps to a different player sample`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.5f,
                animatedProgress = 0.2f,
                isPlaying = false,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `paused cursor does not snap when it already matches the player`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.5f,
                animatedProgress = 0.5f,
                isPlaying = false,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
    }

    @Test
    fun `playing cursor ignores drift under the sync threshold`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.21f,
                animatedProgress = 0.2f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
    }

    @Test
    fun `playing cursor snaps when the player is ahead beyond the sync threshold`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.3f,
                animatedProgress = 0.2f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `playing cursor does not snap back when the player lags behind the animation`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0f,
                animatedProgress = 0.05f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
    }

    @Test
    fun `playing cursor snaps back when playback restarts from the beginning`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0f,
                animatedProgress = 1f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `stale player samples are ignored until they catch up to a pending seek`() {
        assertThat(
            shouldApplyPlayerProgress(
                playbackProgress = 0.2f,
                pendingSeek = 0.8f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
        assertThat(
            shouldApplyPlayerProgress(
                playbackProgress = 0.8f,
                pendingSeek = 0.8f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `player samples apply immediately when there is no pending seek`() {
        assertThat(
            shouldApplyPlayerProgress(
                playbackProgress = 0.2f,
                pendingSeek = null,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `playing cursor snaps when duration is unknown`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.5f,
                animatedProgress = 0.2f,
                isPlaying = true,
                durationMs = 0,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `playing cursor does not snap when drift is exactly the sync threshold`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.22f,
                animatedProgress = 0.2f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
    }

    @Test
    fun `playing cursor snaps back at the restart progress boundary`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.01f,
                animatedProgress = 0.9f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.011f,
                animatedProgress = 0.9f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0f,
                animatedProgress = 0.89f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isFalse()
    }

    @Test
    fun `faster playback treats the same progress delta as a smaller time gap`() {
        assertThat(
            shouldSnapToPlayer(
                playbackProgress = 0.3f,
                animatedProgress = 0.2f,
                isPlaying = true,
                durationMs = 10_000,
                playbackSpeed = 8f,
            )
        ).isFalse()
    }

    @Test
    fun `a pending seek is acknowledged when duration is unknown and the sample is close`() {
        assertThat(
            hasCaughtUpToSeek(
                playbackProgress = 0.8f,
                pendingSeek = 0.8f,
                durationMs = 0,
                playbackSpeed = 1f,
            )
        ).isTrue()
        assertThat(
            hasCaughtUpToSeek(
                playbackProgress = 0.82f,
                pendingSeek = 0.8f,
                durationMs = 0,
                playbackSpeed = 1f,
            )
        ).isFalse()
        assertThat(
            shouldApplyPlayerProgress(
                playbackProgress = 0.805f,
                pendingSeek = 0.8f,
                durationMs = 0,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `a pending seek is acknowledged at the exact sync threshold`() {
        assertThat(
            hasCaughtUpToSeek(
                playbackProgress = 0.82f,
                pendingSeek = 0.8f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isTrue()
    }

    @Test
    fun `zero playback speed still produces a finite remaining animation duration`() {
        assertThat(
            remainingPlaybackAnimationMs(
                progress = 0.5f,
                durationMs = 10_000,
                playbackSpeed = 0f,
            )
        ).isEqualTo(500_000)
    }

    @Test
    fun `remaining animation duration is zero when the cursor is already at the end`() {
        assertThat(
            remainingPlaybackAnimationMs(
                progress = 1f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isEqualTo(0)
        assertThat(
            remainingPlaybackAnimationMs(
                progress = 1.2f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isEqualTo(0)
    }

    @Test
    fun `remaining animation duration scales with leftover progress and speed`() {
        assertThat(
            remainingPlaybackAnimationMs(
                progress = 0.25f,
                durationMs = 10_000,
                playbackSpeed = 1f,
            )
        ).isEqualTo(7_500)
        assertThat(
            remainingPlaybackAnimationMs(
                progress = 0.25f,
                durationMs = 10_000,
                playbackSpeed = 2f,
            )
        ).isEqualTo(3_750)
    }
}
