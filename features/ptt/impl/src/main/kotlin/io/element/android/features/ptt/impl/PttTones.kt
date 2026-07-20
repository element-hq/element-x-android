/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.media.AudioManager
import android.media.ToneGenerator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.extensions.runCatchingExceptions

/**
 * Half-duplex PTT audio cues: a short "go" chirp when the floor is granted, a "deny" tone when it is
 * not (REQ-780/781). Uses the built-in [ToneGenerator] so no audio assets are needed.
 */
@SingleIn(AppScope::class)
@Inject
class PttTones {
    // ToneGenerator can throw if audio resources are unavailable; degrade to silent.
    private val toneGenerator: ToneGenerator? by lazy {
        runCatchingExceptions { ToneGenerator(AudioManager.STREAM_VOICE_CALL, VOLUME) }.getOrNull()
    }

    /** Floor acquired — "go" chirp. */
    fun playFloorGranted() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
    }

    /** Floor denied or the attempt failed — "deny" tone. */
    fun playFloorDenied() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, TONE_DURATION_MS)
    }

    private companion object {
        const val VOLUME = 80
        const val TONE_DURATION_MS = 150
    }
}
