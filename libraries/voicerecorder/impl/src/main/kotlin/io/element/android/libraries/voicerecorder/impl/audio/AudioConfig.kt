/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import android.media.AudioFormat
import android.media.MediaRecorder.AudioSource

/**
 * Audio configuration for voice recording.
 *
 * @property source the audio source to use, see constants in [AudioSource]
 * @property format the audio format to use, see [AudioFormat]
 * @property sampleRate the sample rate to use. Ensure this matches the value set in [format].
 * @property bitRate the bitrate in bps
 */
data class AudioConfig(
    val source: Int,
    val format: AudioFormat,
    val sampleRate: SampleRate,
    val bitRate: Int,
)
