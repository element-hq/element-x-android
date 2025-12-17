/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResampleTest {
    @Test
    fun `resample works`() {
        listOf(0.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
        }
        listOf(1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
        }
        listOf(0.0f, 1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
        }
        listOf(0.0f, 0.5f, 1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.15f, 0.3f, 0.45000002f, 0.6f, 0.75f, 0.90000004f, 1.0f, 1.0f, 1.0f))
        }
        List(100) { it.toFloat() }.resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f))
        }
    }
}
