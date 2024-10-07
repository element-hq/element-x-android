/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.AudioReader

class FakeAudioReaderFactory(
    private val audio: List<Audio>
) : AudioReader.Factory {
    override fun create(config: AudioConfig, dispatchers: CoroutineDispatchers): AudioReader {
        return FakeAudioReader(dispatchers, audio)
    }
}
