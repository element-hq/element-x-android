/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.media

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class WaveformNormalisationTest {
    @Test
    fun `an empty waveform is padded to the requested number of samples`() {
        val result = persistentListOf<Float>().normalisedData(30)
        assertThat(result).hasSize(30)
        assertThat(result.toSet()).containsExactly(0f)
    }

    @Test
    fun `an empty waveform stays empty when there is no room to display it`() {
        assertThat(persistentListOf<Float>().normalisedData(0)).isEmpty()
        assertThat(persistentListOf<Float>().normalisedData(-1)).isEmpty()
    }

    @Test
    fun `a waveform shorter than the display count is kept as it is`() {
        val waveform = persistentListOf(0.1f, 0.2f, 0.3f)
        assertThat(waveform.normalisedData(30)).containsExactly(0.1f, 0.2f, 0.3f).inOrder()
    }

    @Test
    fun `a waveform longer than the display count is downsampled`() {
        val waveform = persistentListOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f)
        assertThat(waveform.normalisedData(3)).hasSize(3)
    }
}
