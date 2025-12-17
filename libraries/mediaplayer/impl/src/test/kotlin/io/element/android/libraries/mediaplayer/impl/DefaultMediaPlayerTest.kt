/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.impl

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.mediaplayer.test.FakeAudioFocus
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultMediaPlayerTest {
    private val aMediaId = "mediaId"
    private val aMediaItem = MediaItem.Builder().setMediaId(aMediaId).build()

    @Test
    fun `initial state`() = runTest {
        val sut = createDefaultMediaPlayer()
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
        }
    }

    @Test
    fun `start player will update the current position and pause it will stop`() = runTest {
        val playLambda = lambdaRecorder<Unit> { }
        val pauseLambda = lambdaRecorder<Unit> { }
        val player = FakeSimplePlayer(
            playLambda = playLambda,
            pauseLambda = pauseLambda,
        )
        val requestAudioFocusResult = lambdaRecorder<AudioFocusRequester, () -> Unit, Unit> { _, _ -> }
        val releaseAudioFocusResult = lambdaRecorder<Unit> {}
        val audioFocus = FakeAudioFocus(
            requestAudioFocusResult = requestAudioFocusResult,
            releaseAudioFocusResult = releaseAudioFocusResult
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
            audioFocus = audioFocus,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            sut.play()
            playLambda.assertions().isCalledOnce()
            requestAudioFocusResult.assertions().isCalledOnce()
            player.durationResult = 123L
            player.simulateIsPlayingChanged(true)
            val playingState = awaitItem()
            assertThat(playingState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = true,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = 123,
                )
            )
            player.currentPositionResult = 1L
            assertThat(awaitItem()).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = true,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 1,
                    duration = 123,
                )
            )
            player.currentPositionResult = 2L
            assertThat(awaitItem()).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = true,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 2,
                    duration = 123,
                )
            )
            player.pause()
            pauseLambda.assertions().isCalledOnce()
            player.simulateIsPlayingChanged(false)
            releaseAudioFocusResult.assertions().isCalledOnce()
            assertThat(awaitItem()).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 2,
                    duration = 123,
                )
            )
        }
    }

    @Test
    fun `start player on ended playback will not invoke more methods if current media item is null`() = runTest {
        val playLambda = lambdaRecorder<Unit> { }
        val getCurrentMediaItemLambda = lambdaRecorder<MediaItem?> { null }
        val player = FakeSimplePlayer(
            playLambda = playLambda,
            getCurrentMediaItemLambda = getCurrentMediaItemLambda,
        )
        val requestAudioFocusResult = lambdaRecorder<AudioFocusRequester, () -> Unit, Unit> { _, _ -> }
        val audioFocus = FakeAudioFocus(
            requestAudioFocusResult = requestAudioFocusResult,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
            audioFocus = audioFocus,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            player.playbackStateResult = Player.STATE_ENDED
            sut.play()
            playLambda.assertions().isCalledOnce()
            requestAudioFocusResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `start player on ended playback will invoke more methods if current media item is not null`() = runTest {
        val playLambda = lambdaRecorder<Unit> { }
        val prepareLambda = lambdaRecorder<Unit> { }
        val getCurrentMediaItemLambda = lambdaRecorder<MediaItem?> { aMediaItem }
        val setMediaItemLambda = lambdaRecorder<MediaItem, Long, Unit> { _, _ -> }
        val requestAudioFocusResult = lambdaRecorder<AudioFocusRequester, () -> Unit, Unit> { _, _ -> }
        val audioFocus = FakeAudioFocus(
            requestAudioFocusResult = requestAudioFocusResult,
        )
        val player = FakeSimplePlayer(
            playLambda = playLambda,
            prepareLambda = prepareLambda,
            setMediaItemLambda = setMediaItemLambda,
            getCurrentMediaItemLambda = getCurrentMediaItemLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
            audioFocus = audioFocus,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            player.playbackStateResult = Player.STATE_ENDED
            sut.play()
            setMediaItemLambda.assertions().isCalledOnce().with(
                value(aMediaItem),
                value(0L),
            )
            prepareLambda.assertions().isCalledOnce()
            playLambda.assertions().isCalledOnce()
            requestAudioFocusResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `pause player invokes pause on the embedded player`() = runTest {
        val pauseLambda = lambdaRecorder<Unit> { }
        val player = FakeSimplePlayer(
            pauseLambda = pauseLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.pause()
        pauseLambda.assertions().isCalledOnce()
    }

    @Test
    fun `close player invokes release on the embedded player`() = runTest {
        val releaseLambda = lambdaRecorder<Unit> { }
        val player = FakeSimplePlayer(
            releaseLambda = releaseLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.close()
        releaseLambda.assertions().isCalledOnce()
    }

    @Test
    fun `seekTo invokes release on the embedded player`() = runTest {
        val seekToLambda = lambdaRecorder<Long, Unit> { }
        val player = FakeSimplePlayer(
            seekToLambda = seekToLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.state.test {
            awaitItem()
            player.currentPositionResult = 33L
            sut.seekTo(33L)
            seekToLambda.assertions().isCalledOnce().with(value(33L))
            val finalState = awaitItem()
            assertThat(finalState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 33L,
                    duration = null,
                )
            )
        }
    }

    @Test
    fun `onPlaybackStateChanged update the state`() = runTest {
        val player = FakeSimplePlayer()
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            player.currentPositionResult = 44
            player.durationResult = 123L
            player.simulatePlaybackStateChanged(Player.STATE_READY)
            val readyState = awaitItem()
            assertThat(readyState).isEqualTo(
                MediaPlayer.State(
                    isReady = true,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 44,
                    duration = 123,
                )
            )
            player.simulatePlaybackStateChanged(Player.STATE_ENDED)
            val endedState = awaitItem()
            assertThat(endedState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = true,
                    mediaId = null,
                    currentPosition = 44,
                    duration = 123,
                )
            )
        }
    }

    @Test
    fun `setMedia with timeout error`() = runTest {
        val pauseLambda = lambdaRecorder<Unit> { }
        val clearMediaItemsLambda = lambdaRecorder<Unit> { }
        val setMediaItemLambda = lambdaRecorder<MediaItem, Long, Unit> { _, _ -> }
        val prepareLambda = lambdaRecorder<Unit> { }
        val player = FakeSimplePlayer(
            pauseLambda = pauseLambda,
            clearMediaItemsLambda = clearMediaItemsLambda,
            setMediaItemLambda = setMediaItemLambda,
            prepareLambda = prepareLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            @Suppress("RunCatchingNotAllowed")
            val result = runCatching {
                sut.setMedia("uri", "mediaId", "mimeType", 12)
            }
            pauseLambda.assertions().isCalledOnce()
            clearMediaItemsLambda.assertions().isCalledOnce()
            setMediaItemLambda.assertions().isCalledOnce().with(
                value(MediaItem.Builder().setUri("uri").setMediaId("mediaId").setMimeType("mimeType").build()),
                value(12L),
            )
            prepareLambda.assertions().isCalledOnce()
            assertThat(result.isFailure).isTrue()
            assertThrows(TimeoutCancellationException::class.java) {
                result.getOrThrow()
            }
        }
    }

    @Test
    fun `setMedia success`() = runTest {
        var player: FakeSimplePlayer? = null
        val pauseLambda = lambdaRecorder<Unit> { }
        val clearMediaItemsLambda = lambdaRecorder<Unit> { }
        val setMediaItemLambda = lambdaRecorder<MediaItem, Long, Unit> { _, _ -> }
        val prepareLambda = lambdaRecorder<Unit> {
            player?.simulatePlaybackStateChanged(Player.STATE_READY)
            player?.simulateMediaItemTransition(aMediaItem)
        }
        player = FakeSimplePlayer(
            pauseLambda = pauseLambda,
            clearMediaItemsLambda = clearMediaItemsLambda,
            setMediaItemLambda = setMediaItemLambda,
            prepareLambda = prepareLambda,
        )
        val sut = createDefaultMediaPlayer(
            simplePlayer = player,
        )
        sut.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                MediaPlayer.State(
                    isReady = false,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = null,
                )
            )
            val state = sut.setMedia("uri", "mediaId", "mimeType", 12)
            pauseLambda.assertions().isCalledOnce()
            clearMediaItemsLambda.assertions().isCalledOnce()
            setMediaItemLambda.assertions().isCalledOnce().with(
                value(MediaItem.Builder().setUri("uri").setMediaId("mediaId").setMimeType("mimeType").build()),
                value(12L),
            )
            prepareLambda.assertions().isCalledOnce()

            val finalState = MediaPlayer.State(
                isReady = true,
                isPlaying = false,
                isEnded = false,
                mediaId = "mediaId",
                currentPosition = 0,
                duration = 0,
            )
            assertThat(awaitItem()).isEqualTo(
                MediaPlayer.State(
                    isReady = true,
                    isPlaying = false,
                    isEnded = false,
                    mediaId = null,
                    currentPosition = 0,
                    duration = 0,
                )
            )
            assertThat(awaitItem()).isEqualTo(finalState)
            assertThat(state).isEqualTo(finalState)
        }
    }

    private fun TestScope.createDefaultMediaPlayer(
        simplePlayer: SimplePlayer = FakeSimplePlayer(),
        audioFocus: AudioFocus = FakeAudioFocus(),
    ): DefaultMediaPlayer = DefaultMediaPlayer(
        player = simplePlayer,
        sessionCoroutineScope = backgroundScope,
        audioFocus = audioFocus,
    )
}
