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
import kotlin.math.min
import kotlin.math.sqrt

@ContributesBinding(RoomScope::class)
class DecibelAudioLevelCalculator @Inject constructor() : AudioLevelCalculator {
    companion object {
        private const val REFERENCE_DB = 50.0 // Reference dB for normal conversation
    }

    override fun calculateAudioLevel(buffer: ShortArray): Float {
        val rms = buffer.rootMeanSquare()

        // Convert to decibels and clip
        val db = 20 * log10(rms / REFERENCE_DB)
        val clipped = min(db, REFERENCE_DB)

        // Scale to the range [0.0, 1.0]
        return (clipped / REFERENCE_DB).toFloat()
    }

    private fun ShortArray.rootMeanSquare(): Double {
        // Use Double to avoid overflow
        val sumOfSquares: Double = sumOf { it.toDouble() * it.toDouble() }
        val avgSquare = sumOfSquares / size.toDouble()
        return sqrt(avgSquare)
    }
}
