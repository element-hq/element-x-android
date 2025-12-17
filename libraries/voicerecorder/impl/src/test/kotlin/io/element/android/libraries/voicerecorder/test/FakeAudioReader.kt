/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
