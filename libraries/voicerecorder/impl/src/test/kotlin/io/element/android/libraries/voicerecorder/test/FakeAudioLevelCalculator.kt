/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.voicerecorder.impl.audio.AudioLevelCalculator
import kotlin.math.abs

class FakeAudioLevelCalculator : AudioLevelCalculator {
    override fun calculateAudioLevel(buffer: ShortArray): Float {
        return buffer.map { abs(it.toFloat()) }.average().toFloat() / Short.MAX_VALUE
    }
}
