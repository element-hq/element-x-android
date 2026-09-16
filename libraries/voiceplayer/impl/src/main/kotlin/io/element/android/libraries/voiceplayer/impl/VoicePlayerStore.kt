/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface VoicePlayerStore {
    suspend fun setPlayBackSpeedIndex(index: Int)
    fun playBackSpeedIndex(): Flow<Int>
}

@ContributesBinding(AppScope::class)
class PreferencesVoicePlayerStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : VoicePlayerStore {
    private val store = preferenceDataStoreFactory.create("elementx_voice_player")
    private val playbackSpeedIndex = intPreferencesKey("playback_speed_index")

    override fun playBackSpeedIndex(): Flow<Int> {
        return store.data.map { prefs ->
            prefs[playbackSpeedIndex] ?: 0
        }
    }

    override suspend fun setPlayBackSpeedIndex(index: Int) {
        store.edit { prefs ->
            prefs[playbackSpeedIndex] = index
        }
    }
}
