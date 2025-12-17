/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import androidx.annotation.FloatRange

interface AudioLevelCalculator {
    /**
     * Calculate the audio level of the audio buffer.
     *
     * @param buffer The audio buffer containing 16bit PCM audio data.
     * @return A float value between 0 and 1 proportional to the audio level.
     */
    @FloatRange(from = 0.0, to = 1.0)
    fun calculateAudioLevel(buffer: ShortArray): Float
}
