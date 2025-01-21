/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

interface AudioLevelCalculator {
    /**
     * Calculate the audio level of the audio buffer.
     *
     * @param buffer The audio buffer containing 16bit PCM audio data.
     * @return A float value between 0 and 1 proportional to the audio level.
     */
    fun calculateAudioLevel(buffer: ShortArray): Float
}
