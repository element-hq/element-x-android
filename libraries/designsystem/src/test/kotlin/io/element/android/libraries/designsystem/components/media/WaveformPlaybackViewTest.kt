/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.designsystem.components.media

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class WaveformPlaybackViewTest : RobolectricTest() {
    @Test
    fun `seeking can be disabled`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(seekEnabled = false)
        waitForIdle()
    }

    @Test
    fun `a paused cursor follows a new player sample`() = runAndroidComposeUiTest {
        val playbackProgress = mutableFloatStateOf(0.2f)
        setWaveformPlaybackView(playbackProgress = { playbackProgress.value })
        playbackProgress.value = 0.8f
        waitForIdle()
    }

    @Test
    fun `playing with a positive duration animates the cursor`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(
            playbackProgress = { 0f },
            isPlaying = true,
            durationMs = 50L,
        )
        waitForIdle()
    }

    @Test
    fun `playing at the end of the media snaps the cursor without tweening`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(
            playbackProgress = { 1f },
            isPlaying = true,
            durationMs = 1_000L,
        )
        waitForIdle()
    }

    @Test
    fun `playing with an unknown duration does not animate`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(
            playbackProgress = { 0.3f },
            isPlaying = true,
            durationMs = 0L,
        )
        waitForIdle()
    }

    @Test
    fun `playing cursor keeps interpolating when the player is only slightly ahead`() = runAndroidComposeUiTest {
        val playbackProgress = mutableFloatStateOf(0.2f)
        setWaveformPlaybackView(
            playbackProgress = { playbackProgress.value },
            isPlaying = true,
            durationMs = 10_000L,
        )
        playbackProgress.value = 0.21f
        waitForIdle()
    }

    @Test
    fun `an empty waveform still renders with a cursor`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(
            waveform = persistentListOf(),
            showCursor = true,
        )
        waitForIdle()
    }

    @Test
    fun `a short waveform still renders`() = runAndroidComposeUiTest {
        setWaveformPlaybackView(waveform = persistentListOf(0.2f, 0.4f, 0.6f))
        waitForIdle()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setWaveformPlaybackView(
    playbackProgress: () -> Float = { 0.5f },
    showCursor: Boolean = false,
    waveform: ImmutableList<Float> = WaveFormSamples.realisticWaveForm,
    isPlaying: Boolean = false,
    durationMs: Long = 0L,
    playbackSpeed: Float = 1f,
    seekEnabled: Boolean = true,
) {
    setSafeContent {
        ElementTheme {
            WaveformPlaybackView(
                playbackProgress = playbackProgress(),
                showCursor = showCursor,
                waveform = waveform,
                onSeek = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                isPlaying = isPlaying,
                durationMs = durationMs,
                playbackSpeed = playbackSpeed,
                seekEnabled = seekEnabled,
            )
        }
    }
}
