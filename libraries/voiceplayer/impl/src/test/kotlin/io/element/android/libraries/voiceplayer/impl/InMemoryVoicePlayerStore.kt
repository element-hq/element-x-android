/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class InMemoryVoicePlayerStore(
    defaultPlaybackSpeedIndex: Int = 0,
) : VoicePlayerStore {
    private val playBackSpeedIndex = MutableStateFlow(defaultPlaybackSpeedIndex)

    override fun playBackSpeedIndex(): Flow<Int> {
        return playBackSpeedIndex.asStateFlow()
    }

    override suspend fun setPlayBackSpeedIndex(index: Int) {
        playBackSpeedIndex.emit(index)
    }
}
