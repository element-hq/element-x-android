/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.preferences.api.store.NicknameStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultNicknameStore(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : NicknameStore {
    companion object {
        private const val NICKNAMES_KEY = "nicknames"
        
        fun storeFile(context: Context): File {
            return context.preferencesDataStoreFile("nicknames_preferences")
        }
    }

    private val nicknamesKey = stringPreferencesKey(NICKNAMES_KEY)
    private val dataStoreFile = storeFile(context)
    private val store = PreferenceDataStoreFactory.create(
        scope = coroutineScope,
    ) { dataStoreFile }

    override suspend fun setNickname(userId: UserId, nickname: String?) {
        store.edit { prefs ->
            val currentMap = prefs[nicknamesKey]?.let { 
                runCatching { Json.decodeFromString<Map<String, String>>(it) }.getOrElse { emptyMap() }
            } ?: emptyMap()
            
            val newMap = if (nickname.isNullOrBlank()) {
                currentMap - userId.value
            } else {
                currentMap + (userId.value to nickname)
            }
            
            prefs[nicknamesKey] = Json.encodeToString(newMap)
        }
    }

    override fun getNickname(userId: UserId): Flow<String?> {
        return store.data.map { prefs ->
            prefs[nicknamesKey]?.let {
                runCatching { Json.decodeFromString<Map<String, String>>(it) }.getOrNull()
            }?.get(userId.value)
        }
    }

    override fun getAllNicknames(): Flow<Map<String, String>> {
        return store.data.map { prefs ->
            prefs[nicknamesKey]?.let {
                runCatching { Json.decodeFromString<Map<String, String>>(it) }.getOrElse { emptyMap() }
            } ?: emptyMap()
        }
    }

    override suspend fun clear() {
        store.edit { prefs -> prefs.clear() }
    }
}
