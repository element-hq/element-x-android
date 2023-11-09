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

import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.features.messages.impl.voicemessages.timeline.DefaultVoiceMessagePlayer
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageMediaRepo
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultVoiceMessagePlayerTest {

    @Test
    fun `initial state`() = runTest {
        createDefaultVoiceMessagePlayer().state.test {
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isMyMedia).isEqualTo(false)
                Truth.assertThat(it.currentPosition).isEqualTo(0)
            }
        }
    }

    @Test
    fun `downloading and play works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isSuccess).isTrue()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isMyMedia).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(0)
            }
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(true)
                Truth.assertThat(it.isMyMedia).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `downloading and play fails`() = runTest {
        val player = createDefaultVoiceMessagePlayer(
            voiceMessageMediaRepo = FakeVoiceMessageMediaRepo().apply {
                shouldFail = true
            },
        )
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isFailure).isTrue()
        }
    }

    @Test
    fun `play fails with no eventId`() = runTest {
        val player = createDefaultVoiceMessagePlayer(
            eventId = null
        )
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isFailure).isTrue()
        }
    }

    @Test
    fun `pause playing works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isSuccess).isTrue()
            skipItems(2) // skip play states
            player.pause()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(false)
                Truth.assertThat(it.isMyMedia).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(1000)
            }
        }
    }

    @Test
    fun `play after pause works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isSuccess).isTrue()
            skipItems(2) // skip play states
            player.pause()
            skipItems(1)
            player.play()
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(true)
                Truth.assertThat(it.isMyMedia).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
            }
        }
    }

    @Test
    fun `seek to works`() = runTest {
        val player = createDefaultVoiceMessagePlayer()
        player.state.test {
            skipItems(1) // skip initial state.
            Truth.assertThat(player.play().isSuccess).isTrue()
            skipItems(2) // skip play states
            player.seekTo(2000)
            awaitItem().let {
                Truth.assertThat(it.isPlaying).isEqualTo(true)
                Truth.assertThat(it.isMyMedia).isEqualTo(true)
                Truth.assertThat(it.currentPosition).isEqualTo(2000)
            }
        }
    }
}

private fun createDefaultVoiceMessagePlayer(
    mediaPlayer: MediaPlayer = FakeMediaPlayer(),
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
