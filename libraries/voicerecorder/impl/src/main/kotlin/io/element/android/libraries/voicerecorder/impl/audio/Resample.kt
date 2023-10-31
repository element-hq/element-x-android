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

/**
 * Resamples [this] list to [size] using linear interpolation.
 */
fun List<Float>.resample(size: Int): List<Float> {
    require(size > 0)
    val input = this
    if (input.isEmpty()) return List(size) { 0f } // fast path.
    if (input.size == 1) return List(size) { input[0] } // fast path.
    if (input.size == size) return this // fast path.
    val step: Float = input.size.toFloat() / size.toFloat()
    return buildList(size) {
        for (i in 0 until size) {
            val x0 = (i * step).toInt()
            val x1 = (x0 + 1).coerceAtMost(input.size - 1)
            val x = i * step - x0
            val y = input[x0] * (1 - x) + input[x1] * x
            add(i, y)
        }
    }
}
