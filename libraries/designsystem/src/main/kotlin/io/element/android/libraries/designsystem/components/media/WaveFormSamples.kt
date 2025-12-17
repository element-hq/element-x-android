/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

object WaveFormSamples {
    val allRangeWaveForm = List(100) { it.toFloat() / 100 }.toImmutableList()

    @Suppress("ktlint:standard:argument-list-wrapping")
    val realisticWaveForm = persistentListOf(
        0.000f, 0.000f, 0.000f, 0.003f, 0.354f,
        0.353f, 0.365f, 0.790f, 0.787f, 0.167f,
        0.333f, 0.975f, 0.000f, 0.102f, 0.003f,
        0.531f, 0.584f, 0.317f, 0.140f, 0.475f,
        0.496f, 0.561f, 0.042f, 0.263f, 0.169f,
        0.829f, 0.349f, 0.010f, 0.000f, 0.000f,
        1.000f, 0.334f, 0.321f, 0.011f, 0.000f,
        0.000f, 0.003f,
    )

    val longRealisticWaveForm = List(4) { realisticWaveForm }.flatten().toImmutableList()
}
