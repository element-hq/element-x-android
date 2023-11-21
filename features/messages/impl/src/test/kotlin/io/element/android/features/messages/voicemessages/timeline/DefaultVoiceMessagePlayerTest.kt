/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.voicemessages.timeline

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.messages.impl.voicemessages.timeline.DefaultVoiceMessagePlayer
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageMediaRepo
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessagePlayer
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultVoiceMessagePlayerTest {

    @Test
    fun `initial state`() = runTest {
        createDefaultVoiceMessagePlayer().state.test {
            matchInitialState()
        }
    }

    @Test
    fun `prepare succeeds`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState()
        }
    }

    @Test
    fun `prepare fails when repo fails`() = runTest {
        val player = createDefaultVoiceMessagePlayer(
            voiceMessageMediaRepo = FakeVoiceMessageMediaRepo().apply {
                shouldFail = true
            },
        )
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isFailure).isTrue()
        }
    }

    @Test
    fun `prepare fails with no eventId`() = runTest {
        val player = createDefaultVoiceMessagePlayer(
            eventId = null
        )
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isFailure).isTrue()
        }
    }

    @Test
    fun `play after prepare works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState()
            player.play()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `play reaches end of media`() = runTest {
        val player = createDefaultVoiceMessagePlayer(
            mediaPlayer = FakeMediaPlayer(
                fakeTotalDurationMs = 1000,
                fakePlayedDurationMs = 1000
            )
        )
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState(fakeTotalDurationMs = 1000)
            player.play()
            awaitItem().let {
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `player1 plays again after both player1 and player2 are finished`() = runTest {
        val mediaPlayer = FakeMediaPlayer(
            fakeTotalDurationMs = 1_000L,
            fakePlayedDurationMs = 1_000L,
        )
        val player1 = createDefaultVoiceMessagePlayer(mediaPlayer = mediaPlayer)
        val player2 = createDefaultVoiceMessagePlayer(mediaPlayer = mediaPlayer)

        // Play player1 until the end.
        player1.state.test {
            matchInitialState()
            Truth.assertThat(player1.prepare().isSuccess).isTrue()
            matchReadyState(1_000L)
            player1.play()
            awaitItem().let { // it plays until the end.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
        }

        // Play player2 until the end.
        player2.state.test {
            matchInitialState()
            Truth.assertThat(player2.prepare().isSuccess).isTrue()
            awaitItem().let { // Additional spurious state due to MediaPlayer owner change.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
            awaitItem().let {// Additional spurious state due to MediaPlayer owner change.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(0)
                Truth.assertThat(it.duration).isEqualTo(null)
            }
            matchReadyState(1_000L)
            player2.play()
            awaitItem().let { // it plays until the end.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
        }

        // Play player1 again.
        player1.state.test {
            awaitItem().let {// Last previous state/
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
            Truth.assertThat(player1.prepare().isSuccess).isTrue()
            awaitItem().let {// Additional spurious state due to MediaPlayer owner change.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(0)
                Truth.assertThat(it.duration).isEqualTo(null)
            }
            matchReadyState(1_000L)
            player1.play()
            awaitItem().let { // it played again until the end.
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
                Truth.assertThat(it.duration).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `pause after play pauses`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState()
            player.play()
            skipItems(1) // skip play state
            player.pause()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `play after pause works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState()
            player.play()
            skipItems(1) // skip play state
            player.pause()
            skipItems(1) // skip pause state
            player.play()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
            }
        }
    }

    @Test
    fun `seek before prepare works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            player.seekTo(2000)
            awaitItem().let {
                Truth.assertThat(it.isReady).isEqualTo(false)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
                Truth.assertThat(it.duration).isEqualTo(null)
            }
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            awaitItem().let {
                Truth.assertThat(it.isReady).isEqualTo(true)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
                Truth.assertThat(it.duration).isEqualTo(FAKE_TOTAL_DURATION_MS)
            }
        }
    }

    @Test
    fun `seek after prepare works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            matchInitialState()
            Truth.assertThat(player.prepare().isSuccess).isTrue()
            matchReadyState()
            player.seekTo(2000)
            awaitItem().let {
                Truth.assertThat(it.isReady).isEqualTo(true)
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isEnded).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
                Truth.assertThat(it.duration).isEqualTo(FAKE_TOTAL_DURATION_MS)
            }
        }
    }
}

private const val FAKE_TOTAL_DURATION_MS = 10_000L
private const val FAKE_PLAYED_DURATION_MS = 1000L

private fun createDefaultVoiceMessagePlayer(
    mediaPlayer: MediaPlayer = FakeMediaPlayer(
        fakeTotalDurationMs = FAKE_TOTAL_DURATION_MS,
        fakePlayedDurationMs = FAKE_PLAYED_DURATION_MS
    ),
    voiceMessageMediaRepo: VoiceMessageMediaRepo = FakeVoiceMessageMediaRepo(),
    eventId: EventId? = AN_EVENT_ID,
) = DefaultVoiceMessagePlayer(
    mediaPlayer = mediaPlayer,
    voiceMessageMediaRepoFactory = { _, _, _ -> voiceMessageMediaRepo },
    eventId = eventId,
    mediaSource = MediaSource(
        url = MXC_URI,
        json = null
    ),
    mimeType = "audio/ogg",
    body = "someBody.ogg"
)

private const val MXC_URI = "mxc://matrix.org/1234567890abcdefg"

private suspend fun TurbineTestContext<VoiceMessagePlayer.State>.matchInitialState() {
    awaitItem().let {
        Truth.assertThat(it.isReady).isEqualTo(false)
        Truth.assertThat(it.isPlaying).isEqualTo(false)
        Truth.assertThat(it.isEnded).isEqualTo(false)
        Truth.assertThat(it.currentPosition).isEqualTo(0)
        Truth.assertThat(it.duration).isEqualTo(null)
    }
}

private suspend fun TurbineTestContext<VoiceMessagePlayer.State>.matchReadyState(
    fakeTotalDurationMs: Long = FAKE_TOTAL_DURATION_MS,
) {
    awaitItem().let {
        Truth.assertThat(it.isReady).isEqualTo(true)
        Truth.assertThat(it.isPlaying).isEqualTo(false)
        Truth.assertThat(it.isEnded).isEqualTo(false)
        Truth.assertThat(it.currentPosition).isEqualTo(0)
        Truth.assertThat(it.duration).isEqualTo(fakeTotalDurationMs)
    }
}
