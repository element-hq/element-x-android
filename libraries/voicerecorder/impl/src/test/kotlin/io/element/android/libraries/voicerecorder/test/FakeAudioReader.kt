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

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioReader
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class FakeAudioReader(
    private val dispatchers: CoroutineDispatchers,
    private val audio: List<Audio>,
) : AudioReader {
    private var isRecording = false
    override suspend fun record(onAudio: suspend (Audio) -> Unit) {
        isRecording = true
        withContext(dispatchers.io) {
            val audios = audio.iterator()
            while (audios.hasNext()) {
                if (!isRecording) break
                onAudio(audios.next())
                yield()
            }
            while (isActive) {
                // do not return from the coroutine until it is cancelled
                yield()
            }
        }
    }

    override fun stop() {
        isRecording = false
    }
}
