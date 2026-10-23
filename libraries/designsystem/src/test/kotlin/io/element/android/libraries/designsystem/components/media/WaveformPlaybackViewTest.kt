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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
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
    fun `dragging the cursor reports the seek progress, then the seek`() = runAndroidComposeUiTest {
        val seekProgressChanges = mutableListOf<Float?>()
        val seeks = mutableListOf<Float>()
        setWaveformPlaybackView(
            // An empty waveform fills the whole width.
            waveform = persistentListOf(),
            onSeekProgressChange = { seekProgressChanges += it },
            onSeek = { seeks += it },
        )
        onRoot().performTouchInput { down(centerLeft + Offset(1f, 0f)) }
        onRoot().performTouchInput { moveTo(center) }
        onRoot().performTouchInput { up() }
        assertThat(seekProgressChanges).hasSize(3)
        assertThat(seekProgressChanges[0]).isWithin(0.05f).of(0f)
        assertThat(seekProgressChanges[1]).isWithin(0.05f).of(0.5f)
        assertThat(seekProgressChanges[2]).isNull()
        assertThat(seeks).containsExactly(seekProgressChanges[1])
    }

    @Test
    fun `playing renders the waveform`() = runAndroidComposeUiTest {
        // The progress is updated on every frame while playing, so Compose never gets idle.
        mainClock.autoAdvance = false
        setWaveformPlaybackView(
            playbackProgress = { 0f },
            isPlaying = true,
            durationMs = 1_000L,
            showCursor = true,
        )
        mainClock.advanceTimeBy(500L)
    }

    @Test
    fun `smooth progress follows a new player sample when paused`() = runAndroidComposeUiTest {
        val playbackProgress = mutableFloatStateOf(0.2f)
        val smoothProgress = setSmoothPlaybackProgress(playbackProgress = { playbackProgress.floatValue })
        assertThat(smoothProgress.progress).isEqualTo(0.2f)
        playbackProgress.floatValue = 0.8f
        // No frame is requested while paused, so notify the change before advancing the clock.
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeByFrame()
        assertThat(smoothProgress.progress).isEqualTo(0.8f)
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isEqualTo(0.8f)
    }

    @Test
    fun `smooth progress advances between player samples while playing`() = runAndroidComposeUiTest {
        val smoothProgress = setSmoothPlaybackProgress(
            playbackProgress = { 0.2f },
            isPlaying = true,
            durationMs = 10_000L,
            playbackSpeed = 2f,
        )
        mainClock.advanceTimeBy(1_000L)
        // 1 second at 2x speed of a 10 second media is 0.2 of progress. Allow a few frames of error.
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.4f)
    }

    @Test
    fun `smooth progress restarts from each new player sample`() = runAndroidComposeUiTest {
        val playbackProgress = mutableFloatStateOf(0.5f)
        val smoothProgress = setSmoothPlaybackProgress(
            playbackProgress = { playbackProgress.floatValue },
            isPlaying = true,
            durationMs = 10_000L,
        )
        mainClock.advanceTimeBy(2_000L)
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.7f)
        // The player is behind the estimate: follow the player.
        playbackProgress.floatValue = 0.6f
        mainClock.advanceTimeByFrame()
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.6f)
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.7f)
    }

    @Test
    fun `smooth progress continues from a seek while playing`() = runAndroidComposeUiTest {
        val smoothProgress = setSmoothPlaybackProgress(
            playbackProgress = { 0.2f },
            isPlaying = true,
            durationMs = 10_000L,
        )
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.3f)
        smoothProgress.seekTo(0.8f)
        assertThat(smoothProgress.progress).isEqualTo(0.8f)
        // The player has not reported the new position yet: the progress must not go back to where it was playing.
        mainClock.advanceTimeByFrame()
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.8f)
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isWithin(0.01f).of(0.9f)
    }

    @Test
    fun `smooth progress continues from a seek while paused`() = runAndroidComposeUiTest {
        val smoothProgress = setSmoothPlaybackProgress(playbackProgress = { 0.2f })
        smoothProgress.seekTo(0.8f)
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isEqualTo(0.8f)
    }

    @Test
    fun `smooth progress only follows the player samples when animations are disabled`() {
        val motionDurationScale = TestMotionDurationScale(scaleFactor = 0f)
        runAndroidComposeUiTest(effectContext = motionDurationScale) {
            val playbackProgress = mutableFloatStateOf(0.2f)
            val smoothProgress = setSmoothPlaybackProgress(
                playbackProgress = { playbackProgress.floatValue },
                isPlaying = true,
                durationMs = 10_000L,
            )
            mainClock.advanceTimeBy(1_000L)
            assertThat(smoothProgress.progress).isEqualTo(0.2f)
            playbackProgress.floatValue = 0.3f
            Snapshot.sendApplyNotifications()
            mainClock.advanceTimeByFrame()
            assertThat(smoothProgress.progress).isEqualTo(0.3f)
            mainClock.advanceTimeBy(1_000L)
            assertThat(smoothProgress.progress).isEqualTo(0.3f)
        }
    }

    @Test
    fun `smooth progress restarts when animations are enabled again`() {
        val motionDurationScale = TestMotionDurationScale(scaleFactor = 1f)
        runAndroidComposeUiTest(effectContext = motionDurationScale) {
            val playbackProgress = mutableFloatStateOf(0.2f)
            val smoothProgress = setSmoothPlaybackProgress(
                playbackProgress = { playbackProgress.floatValue },
                isPlaying = true,
                durationMs = 10_000L,
            )
            mainClock.advanceTimeBy(1_000L)
            assertThat(smoothProgress.progress).isWithin(0.01f).of(0.3f)
            // Disabling animations goes back to the latest player sample.
            motionDurationScale.scaleFactor = 0f
            Snapshot.sendApplyNotifications()
            mainClock.advanceTimeByFrame()
            assertThat(smoothProgress.progress).isEqualTo(0.2f)
            playbackProgress.floatValue = 0.5f
            Snapshot.sendApplyNotifications()
            mainClock.advanceTimeBy(1_000L)
            assertThat(smoothProgress.progress).isEqualTo(0.5f)
            // Enabling animations again extrapolates from the latest player sample.
            motionDurationScale.scaleFactor = 1f
            Snapshot.sendApplyNotifications()
            mainClock.advanceTimeBy(1_000L)
            assertThat(smoothProgress.progress).isWithin(0.01f).of(0.6f)
        }
    }

    @Test
    fun `smooth progress stops at the end of the media`() = runAndroidComposeUiTest {
        val smoothProgress = setSmoothPlaybackProgress(
            playbackProgress = { 0.9f },
            isPlaying = true,
            durationMs = 1_000L,
        )
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isEqualTo(1f)
    }

    @Test
    fun `smooth progress does not advance with an unknown duration`() = runAndroidComposeUiTest {
        val smoothProgress = setSmoothPlaybackProgress(
            playbackProgress = { 0.3f },
            isPlaying = true,
            durationMs = 0L,
        )
        mainClock.advanceTimeBy(1_000L)
        assertThat(smoothProgress.progress).isEqualTo(0.3f)
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

private class TestMotionDurationScale(scaleFactor: Float) : MotionDurationScale {
    override var scaleFactor by mutableFloatStateOf(scaleFactor)
}

/**
 * Sets [rememberSmoothPlaybackProgressState] as content and returns the state.
 * The main clock is not advanced automatically.
 */
private fun AndroidComposeUiTest<ComponentActivity>.setSmoothPlaybackProgress(
    playbackProgress: () -> Float,
    isPlaying: Boolean = false,
    durationMs: Long = 0L,
    playbackSpeed: Float = 1f,
): SmoothPlaybackProgressState {
    mainClock.autoAdvance = false
    var state: SmoothPlaybackProgressState? = null
    setSafeContent {
        state = rememberSmoothPlaybackProgressState(
            playbackProgress = playbackProgress(),
            isPlaying = isPlaying,
            durationMs = durationMs,
            playbackSpeed = playbackSpeed,
        )
    }
    mainClock.advanceTimeByFrame()
    return requireNotNull(state)
}

private fun AndroidComposeUiTest<ComponentActivity>.setWaveformPlaybackView(
    playbackProgress: () -> Float = { 0.5f },
    showCursor: Boolean = false,
    waveform: ImmutableList<Float> = WaveFormSamples.realisticWaveForm,
    isPlaying: Boolean = false,
    durationMs: Long = 0L,
    playbackSpeed: Float = 1f,
    seekEnabled: Boolean = true,
    onSeek: (Float) -> Unit = {},
    onSeekProgressChange: (Float?) -> Unit = {},
) {
    setSafeContent {
        ElementTheme {
            WaveformPlaybackView(
                playbackProgress = playbackProgress(),
                showCursor = showCursor,
                waveform = waveform,
                onSeek = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                isPlaying = isPlaying,
                durationMs = durationMs,
                playbackSpeed = playbackSpeed,
                seekEnabled = seekEnabled,
                onSeekProgressChange = onSeekProgressChange,
            )
        }
    }
}
