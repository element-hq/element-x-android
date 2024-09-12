/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import io.element.android.opusencoder.configuration.SampleRate as LibOpusOggSampleRate

data object SampleRate {
    const val HZ = 48_000
    fun asEncoderModel() = LibOpusOggSampleRate.Rate48kHz
}
