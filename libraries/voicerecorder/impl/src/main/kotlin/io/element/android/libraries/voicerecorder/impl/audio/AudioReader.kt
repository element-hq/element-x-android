/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import io.element.android.libraries.core.coroutine.CoroutineDispatchers

interface AudioReader {
    /**
     * Record audio data continuously.
     *
     * @param onAudio callback when audio is read.
     */
    suspend fun record(
        onAudio: suspend (Audio) -> Unit,
    )

    fun stop()

    interface Factory {
        fun create(config: AudioConfig, dispatchers: CoroutineDispatchers): AudioReader
    }
}
