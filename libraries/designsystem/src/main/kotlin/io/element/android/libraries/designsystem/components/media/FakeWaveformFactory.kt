/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

/**
 * Generate a waveform for testing purposes.
 *
 * The waveform is a list of floats between 0 and 1.
 *
 * @param length The length of the waveform.
 */
fun createFakeWaveform(length: Int = 1000): ImmutableList<Float> {
    val random = Random(seed = 2)
    return List(length) { random.nextFloat() }
        .toPersistentList()
}
