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

package io.element.android.libraries.voicerecorder.impl

import android.media.AudioFormat
import android.media.MediaRecorder
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.di.VoiceRecorderModule
import io.element.android.libraries.voicerecorder.test.FakeAudioLevelCalculator
import io.element.android.libraries.voicerecorder.test.FakeAudioRecorderFactory
import io.element.android.libraries.voicerecorder.test.FakeEncoder
import io.element.android.libraries.voicerecorder.test.FakeFileSystem
import io.element.android.libraries.voicerecorder.test.FakeVoiceFileManager
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class VoiceRecorderImplTest {
    private val fakeFileSystem = FakeFileSystem()

    @Test
    fun `it emits the initial state`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)
        }
    }

    @Test
    fun `when recording, it emits the recording state`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord("room-id")
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(1.0))
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(0.0))
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(1.0))
        }
    }

    @Test
    fun `when stopped, it provides a file`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord(ROOM_ID)
            skipItems(3)
            voiceRecorder.stopRecord()
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Finished(File(FILE_PATH), "audio/ogg"))
            assertThat(fakeFileSystem.files[File(FILE_PATH)]).isEqualTo(ENCODED_DATA)
        }
    }

    @Test
    fun `when cancelled, it deletes the file`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord(ROOM_ID)
            skipItems(3)
            voiceRecorder.stopRecord(cancelled = true)
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)
            assertThat(fakeFileSystem.files[File(FILE_PATH)]).isNull()
        }
    }

    private fun TestScope.createVoiceRecorder(): VoiceRecorderImpl {
        val fileConfig = VoiceRecorderModule.provideVoiceFileConfig()
        return VoiceRecorderImpl(
            dispatchers = testCoroutineDispatchers(),
            audioReaderFactory = FakeAudioRecorderFactory(
                audio = AUDIO,
            ),
            encoder = FakeEncoder(fakeFileSystem),
            config = AudioConfig(
                format = AUDIO_FORMAT,
                bitRate = 24 * 1000,
                source = MediaRecorder.AudioSource.MIC,
            ),
            fileConfig = fileConfig,
            fileManager = FakeVoiceFileManager(fakeFileSystem, fileConfig),
            audioLevelCalculator = FakeAudioLevelCalculator(),
            appCoroutineScope = backgroundScope,
        )
    }

    companion object {
        const val ROOM_ID = "room-id"
        const val FILE_PATH = "voice_recordings/room-id.ogg"
        private lateinit var AUDIO_FORMAT: AudioFormat

        // FakeEncoder doesn't actually encode, it just writes the data to the file
        private const val ENCODED_DATA = "[32767, 32767, 32767][32767, 32767, 32767]"
        private const val MAX_AMP = Short.MAX_VALUE
        private val AUDIO = listOf(
            Audio.Data(3, shortArrayOf(MAX_AMP, MAX_AMP, MAX_AMP)),
            Audio.Error(-1),
            Audio.Data(3, shortArrayOf(MAX_AMP, MAX_AMP, MAX_AMP)),
        )

        @BeforeClass
        @JvmStatic
        fun initAudioFormat() {
            AUDIO_FORMAT = mockk()
        }
    }
}
