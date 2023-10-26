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

package io.element.android.libraries.designsystem.components.media

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.roundToInt

data class Waveform (
    val data: ImmutableList<Int>
) {
    companion object {
        private val dataRange = 0..1024
    }

    fun normalisedData(maxSamplesCount: Int): ImmutableList<Float> {
        if(maxSamplesCount <= 0) {
            return persistentListOf()
        }

        // Filter the data to keep only the expected number of samples
        val result = if (data.size > maxSamplesCount) {
            (0..<maxSamplesCount)
                .map { index ->
                    val targetIndex = (index.toDouble() * (data.count().toDouble() / maxSamplesCount.toDouble())).roundToInt()
                    data[targetIndex]
                }
        } else {
            data
        }

        // Normalize the sample in the allowed range
        return result.map { it.toFloat() / dataRange.last.toFloat() }.toPersistentList()
    }
}
