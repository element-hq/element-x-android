/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.voiceplayer.api.AutoplayTimelineItemInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultVoiceMessageAutoplayManagerTest {
    @Test
    fun `when voice message ends and next is voice, autoplay triggers`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aVoiceTimelineItem(eventId = "\$event2"), // index 0 = newest
            aVoiceTimelineItem(eventId = "\$event1"), // index 1 = older
        )
        manager.updateTimelineItems(items)

        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()

        assertThat(fakeTransitionSound.playCount).isEqualTo(1)
    }

    @Test
    fun `when voice message ends and next is not voice, no autoplay`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aTextTimelineItem(eventId = "\$event2"), // index 0 = newest, NOT voice
            aVoiceTimelineItem(eventId = "\$event1"), // index 1 = older
        )
        manager.updateTimelineItems(items)

        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()

        assertThat(fakeTransitionSound.playCount).isEqualTo(0)
    }

    @Test
    fun `when voice message ends and it is the newest item, no autoplay`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aVoiceTimelineItem(eventId = "\$event1"), // index 0 = newest, only item
        )
        manager.updateTimelineItems(items)

        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()

        assertThat(fakeTransitionSound.playCount).isEqualTo(0)
    }

    @Test
    fun `cancelAutoplay prevents next autoplay trigger`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aVoiceTimelineItem(eventId = "\$event2"),
            aVoiceTimelineItem(eventId = "\$event1"),
        )
        manager.updateTimelineItems(items)
        manager.cancelAutoplay()

        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()

        assertThat(fakeTransitionSound.playCount).isEqualTo(0)
    }

    @Test
    fun `autoplay chain works across 3 consecutive voice messages`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aVoiceTimelineItem(eventId = "\$event3"), // newest
            aVoiceTimelineItem(eventId = "\$event2"),
            aVoiceTimelineItem(eventId = "\$event1"), // oldest
        )
        manager.updateTimelineItems(items)

        // event1 ends → should autoplay event2
        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()
        assertThat(fakeTransitionSound.playCount).isEqualTo(1)

        // event2 ends → should autoplay event3
        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event2")
        advanceUntilIdle()
        assertThat(fakeTransitionSound.playCount).isEqualTo(2)

        // event3 ends → no more voice messages
        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event3")
        advanceUntilIdle()
        assertThat(fakeTransitionSound.playCount).isEqualTo(2)
    }

    @Test
    fun `non-voice message breaks the chain`() = runTest {
        val mediaPlayerState = MutableStateFlow(aMediaPlayerState())
        val fakeMediaPlayer = TestableMediaPlayer(mediaPlayerState)
        val fakeTransitionSound = FakeTransitionSoundPlayer()
        val manager = createAutoplayManager(
            mediaPlayer = fakeMediaPlayer,
            transitionSoundPlayer = fakeTransitionSound,
        )

        val items = listOf(
            aVoiceTimelineItem(eventId = "\$event3"), // newest
            aTextTimelineItem(eventId = "\$event2"), // text breaks chain
            aVoiceTimelineItem(eventId = "\$event1"), // oldest
        )
        manager.updateTimelineItems(items)

        mediaPlayerState.value = aMediaPlayerState(isEnded = true, mediaId = "\$event1")
        advanceUntilIdle()

        assertThat(fakeTransitionSound.playCount).isEqualTo(0)
    }

    // -- Helpers --

    private fun TestScope.createAutoplayManager(
        mediaPlayer: MediaPlayer = FakeMediaPlayer(),
        transitionSoundPlayer: TransitionSoundPlayer = FakeTransitionSoundPlayer(),
    ): DefaultVoiceMessageAutoplayManager {
        // Use UnconfinedTestDispatcher so the collect and launched autoplay coroutines
        // run eagerly, allowing assertions to check side effects synchronously.
        val childScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        return DefaultVoiceMessageAutoplayManager(
            mediaPlayer = mediaPlayer,
            voiceMessagePlayerFactory = FakeVoiceMessagePlayerFactory(),
            transitionSoundPlayer = transitionSoundPlayer,
            coroutineScope = childScope,
        )
    }

    private fun aMediaPlayerState(
        isReady: Boolean = false,
        isPlaying: Boolean = false,
        isEnded: Boolean = false,
        mediaId: String? = null,
        currentPosition: Long = 0L,
        duration: Long? = null,
    ) = MediaPlayer.State(
        isReady = isReady,
        isPlaying = isPlaying,
        isEnded = isEnded,
        mediaId = mediaId,
        currentPosition = currentPosition,
        duration = duration,
    )

    private fun aVoiceTimelineItem(eventId: String) = AutoplayTimelineItemInfo(
        eventId = EventId(eventId),
        isVoiceMessage = true,
        mediaSource = MediaSource("mxc://matrix.org/$eventId"),
        mimeType = "audio/ogg",
        filename = "voice.ogg",
        duration = 5.seconds,
    )

    private fun aTextTimelineItem(eventId: String) = AutoplayTimelineItemInfo(
        eventId = EventId(eventId),
        isVoiceMessage = false,
        mediaSource = null,
        mimeType = null,
        filename = null,
        duration = null,
    )
}

/**
 * A simple MediaPlayer wrapper that exposes a controllable state flow.
 */
private class TestableMediaPlayer(
    private val stateFlow: MutableStateFlow<MediaPlayer.State>,
) : MediaPlayer {
    override val state = stateFlow

    override suspend fun setMedia(uri: String, mediaId: String, mimeType: String, startPositionMs: Long): MediaPlayer.State {
        stateFlow.value = stateFlow.value.copy(mediaId = mediaId, isReady = true)
        return stateFlow.value
    }

    override fun play() {
        stateFlow.value = stateFlow.value.copy(isPlaying = true, isEnded = false)
    }

    override fun pause() {
        stateFlow.value = stateFlow.value.copy(isPlaying = false)
    }

    override fun seekTo(positionMs: Long) {
        stateFlow.value = stateFlow.value.copy(currentPosition = positionMs)
    }

    override fun setPlaybackSpeed(speed: Float) = Unit
    override fun close() = Unit
}

/**
 * A fake VoiceMessagePlayer.Factory that creates minimal players for testing autoplay.
 */
private class FakeVoiceMessagePlayerFactory : VoiceMessagePlayer.Factory {
    override fun create(
        eventId: EventId?,
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
    ): VoiceMessagePlayer = FakeVoiceMessagePlayer()
}

private class FakeTransitionSoundPlayer : TransitionSoundPlayer {
    var playCount = 0
        private set

    override suspend fun playAndAwait() {
        playCount++
    }
}

private class FakeVoiceMessagePlayer : VoiceMessagePlayer {
    override val state = MutableStateFlow(
        VoiceMessagePlayer.State(
            isReady = false,
            isPlaying = false,
            isEnded = false,
            currentPosition = 0L,
            duration = null,
        )
    )

    override suspend fun prepare(): Result<Unit> = Result.success(Unit)
    override fun play() = Unit
    override fun pause() = Unit
    override fun seekTo(positionMs: Long) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
}
