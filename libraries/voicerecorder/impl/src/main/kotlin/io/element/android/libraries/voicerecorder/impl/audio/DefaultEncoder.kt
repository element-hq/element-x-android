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

package io.element.android.libraries.voicerecorder.impl.audio

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.opusencoder.OggOpusEncoder
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

/**
 * Safe wrapper for OggOpusEncoder.
 */
@ContributesBinding(RoomScope::class)
class DefaultEncoder @Inject constructor(
    private val encoderProvider: Provider<OggOpusEncoder>,
    config: AudioConfig,
) : Encoder {
    private val bitRate = config.bitRate
    private val sampleRate = config.sampleRate.asEncoderModel()

    private var encoder: OggOpusEncoder? = null
    override fun init(
        file: File,
    ) {
        encoder?.release()
        encoder = encoderProvider.get().apply {
            init(file.absolutePath, sampleRate)
            setBitrate(bitRate)
            // TODO check encoder application: 2048 (voice, default is typically 2049 as audio)
        }
    }

    override fun encode(
        buffer: ShortArray,
        readSize: Int,
    ) {
        encoder?.encode(buffer, readSize)
            ?: Timber.w("Can't encode when encoder not initialized")
    }

    override fun release() {
        encoder?.release()
            ?: Timber.w("Can't release encoder that is not initialized")
        encoder = null
    }
}
