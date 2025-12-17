/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
