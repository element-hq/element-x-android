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
import io.element.android.appconfig.VoiceMessageConfig
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.SampleRate
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class VoiceRecorderImplTest {
    private val fakeFileSystem = FakeFileSystem()
    private val timeSource = TestTimeSource()

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

            voiceRecorder.startRecord()
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(0.seconds, listOf(1.0f)))
            timeSource += 1.seconds
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(1.seconds, listOf()))
            timeSource += 1.seconds
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(2.seconds, listOf(1.0f, 1.0f)))
        }
    }

    @Test
    fun `when elapsed time reaches 30 minutes, it stops recording`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord()
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(0.minutes, listOf(1.0f)))
            timeSource += VoiceMessageConfig.maxVoiceMessageDuration
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Recording(VoiceMessageConfig.maxVoiceMessageDuration, listOf()))
            timeSource += 1.milliseconds

            assertThat(awaitItem()).isEqualTo(
                VoiceRecorderState.Finished(
                    file = File(FILE_PATH),
                    mimeType = MimeTypes.Ogg,
                    waveform = List(100) { 1f },
                    duration = VoiceMessageConfig.maxVoiceMessageDuration,
                )
            )
        }
    }

    @Test
    fun `when stopped, it provides a file and duration`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord()
            skipItems(1)
            timeSource += 5.seconds
            skipItems(2)
            voiceRecorder.stopRecord()
            assertThat(awaitItem()).isEqualTo(
                VoiceRecorderState.Finished(
                    file = File(FILE_PATH),
                    mimeType = MimeTypes.Ogg,
                    waveform = List(100) { 1f },
                    duration = 5.seconds,
                )
            )
            assertThat(fakeFileSystem.files[File(FILE_PATH)]).isEqualTo(ENCODED_DATA)
        }
    }

    @Test
    fun `when cancelled, it deletes the file`() = runTest {
        val voiceRecorder = createVoiceRecorder()
        voiceRecorder.state.test {
            assertThat(awaitItem()).isEqualTo(VoiceRecorderState.Idle)

            voiceRecorder.startRecord()
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
            timeSource = timeSource,
            audioReaderFactory = FakeAudioRecorderFactory(
                audio = AUDIO,
            ),
            encoder = FakeEncoder(fakeFileSystem),
            config = AudioConfig(
                format = AUDIO_FORMAT,
                bitRate = 24_000, // 24 kbps
                sampleRate = SampleRate,
                source = MediaRecorder.AudioSource.MIC,
            ),
            fileConfig = fileConfig,
            fileManager = FakeVoiceFileManager(fakeFileSystem, fileConfig, FILE_ID),
            audioLevelCalculator = FakeAudioLevelCalculator(),
            appCoroutineScope = backgroundScope,
        )
    }

    companion object {
        const val FILE_ID: String = "recording"
        const val FILE_PATH = "voice_recordings/${FILE_ID}.ogg"
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
