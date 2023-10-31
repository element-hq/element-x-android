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
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Default implementation of [AudioLevelCalculator].
 *
 * It computes the normalized [0;1] dBov value of the given PCM16 encoded [ShortArray].
 * See: https://en.wikipedia.org/wiki/DBFS
 */
@ContributesBinding(RoomScope::class)
class DBovAudioLevelCalculator @Inject constructor() : AudioLevelCalculator {
    override fun calculateAudioLevel(buffer: ShortArray): Float {
        return buffer.rms().dBov().normalize().coerceIn(0f, 1f)
    }
}

/**
 * Computes the normalized (range 0.0 to 1.0) root mean square
 * value of the given PCM16 encoded [ShortArray].
 */
private fun ShortArray.rms(): Float {
    val floats = FloatArray(this.size) { i -> this[i] / Short.MAX_VALUE.toFloat() }
    val squared = FloatArray(this.size) { i -> floats[i] * floats[i] }
    val sum = squared.fold(0.0f) { acc, f -> acc + f }
    val average = sum / this.size
    return sqrt(average)
}

/**
 * Converts the given RMS value to decibels relative to overload (dBov).
 * It has range [-96.0, 0.0] where 0.0 is the value of a full scale square wave.
 */
private fun Float.dBov(): Float = 20 * log10(this)

/**
 * Normalizes the given dBov value to the range [0.0, 1.0].
 */
private fun Float.normalize(): Float = (this + DYNAMIC_RANGE_PCM16) / DYNAMIC_RANGE_PCM16

private const val DYNAMIC_RANGE_PCM16: Float = 96.0f
