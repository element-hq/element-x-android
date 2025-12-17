/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Default implementation of [AudioLevelCalculator].
 *
 * It computes the normalized [0;1] dBov value of the given PCM16 encoded [ShortArray].
 * See: https://en.wikipedia.org/wiki/DBFS
 */
@ContributesBinding(RoomScope::class)
class DBovAudioLevelCalculator : AudioLevelCalculator {
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
