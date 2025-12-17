/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.di

import android.media.AudioFormat
import android.media.MediaRecorder
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.SampleRate
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileConfig
import io.element.android.opusencoder.OggOpusEncoder

@BindingContainer
@ContributesTo(RoomScope::class)
object VoiceRecorderModule {
    @Provides
    fun provideAudioConfig(): AudioConfig {
        val sampleRate = SampleRate
        return AudioConfig(
            format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate.HZ)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build(),
            // 24 kbps
            bitRate = 24_000,
            sampleRate = sampleRate,
            source = MediaRecorder.AudioSource.MIC,
        )
    }

    @Provides
    public fun provideVoiceFileConfig(): VoiceFileConfig =
        VoiceFileConfig(
            cacheSubdir = "voice_recordings",
            fileExt = "ogg",
            mimeType = MimeTypes.Ogg,
        )

    @Provides
    fun provideOggOpusEncoder(): OggOpusEncoder = OggOpusEncoder.create()
}
