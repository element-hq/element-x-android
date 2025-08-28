/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@SingleIn(SessionScope::class)
class EmojiHistoryStore @Inject constructor(
    currentSessionIdHolder: CurrentSessionIdHolder,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) {
    private val store = preferenceDataStoreFactory.create("${currentSessionIdHolder.current.value.hash()}_emoji_history")

    suspend fun add(emoji: String) {
        store.edit { preferences ->
            val key = intPreferencesKey(emoji)
            preferences[key] = (preferences[key] ?: 0) + 1
        }
    }

    suspend fun reset() {
        store.updateData { preferences -> preferences.toMutablePreferences().also { it.clear() } }
    }

    fun getAll() = store.data.map { it.toMap() }

    private fun Preferences.toMap(): Map<String, Int> {
        return this.asMap().mapNotNull { (key, value) ->
            val name = key.name
            if (value is Int) {
                name to value
            } else {
                null
            }
        }.toMap()
    }
}
