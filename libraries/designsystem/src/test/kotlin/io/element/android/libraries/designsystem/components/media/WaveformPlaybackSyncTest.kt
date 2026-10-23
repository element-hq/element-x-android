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
    fun `progress is unchanged when no time has elapsed`() {
        assertThat(extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 0L, durationMs = 10_000L, playbackSpeed = 1f)).isEqualTo(0.3f)
    }

    @Test
    fun `progress advances with the elapsed time`() {
        val progress = extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 1_000L, durationMs = 10_000L, playbackSpeed = 1f)
        assertThat(progress).isWithin(0.0001f).of(0.4f)
    }

    @Test
    fun `progress advances faster with a higher playback speed`() {
        val progress = extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 1_000L, durationMs = 10_000L, playbackSpeed = 2f)
        assertThat(progress).isWithin(0.0001f).of(0.5f)
    }

    @Test
    fun `progress does not go past the end`() {
        assertThat(extrapolatePlaybackProgress(progress = 0.9f, elapsedMs = 5_000L, durationMs = 10_000L, playbackSpeed = 1f)).isEqualTo(1f)
    }

    @Test
    fun `progress is unchanged when the duration is unknown`() {
        assertThat(extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 1_000L, durationMs = 0L, playbackSpeed = 1f)).isEqualTo(0.3f)
    }

    @Test
    fun `progress is unchanged with an invalid playback speed`() {
        assertThat(extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 1_000L, durationMs = 10_000L, playbackSpeed = Float.NaN)).isEqualTo(0.3f)
        assertThat(extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = 1_000L, durationMs = 10_000L, playbackSpeed = -1f)).isEqualTo(0.3f)
    }

    @Test
    fun `negative elapsed time does not move progress backward`() {
        assertThat(extrapolatePlaybackProgress(progress = 0.3f, elapsedMs = -1_000L, durationMs = 10_000L, playbackSpeed = 1f)).isEqualTo(0.3f)
    }
}
