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

import android.media.AudioFormat
import io.element.android.opusencoder.configuration.SampleRate

internal fun SampleRate.toHz(): Hz = Hz(value)
@JvmInline
internal value class Hz(val value: Int)
internal fun Hz.toLibModel() =
    when (this) {
        SampleRate.Rate8khz.toHz() -> SampleRate.Rate8khz
        SampleRate.Rate12kHz.toHz() -> SampleRate.Rate12kHz
        SampleRate.Rate16kHz.toHz() -> SampleRate.Rate16kHz
        SampleRate.Rate24KHz.toHz() -> SampleRate.Rate24KHz
        SampleRate.Rate48kHz.toHz() -> SampleRate.Rate48kHz
        else -> throw IllegalArgumentException("Unknown sample rate: $this")
    }

internal fun AudioFormat.sampleRateHz(): Hz = Hz(sampleRate)
