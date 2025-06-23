/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
        encoder = null
    }
}
