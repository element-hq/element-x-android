/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.libraries.matrix.api.search.SearchBackfillStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File

/**
 * Stores the backfill cursor as a JSON blob in its own DataStore file.
 *
 * Deliberately not part of `SessionPreferencesStore`: that holds settings the user chose, this holds
 * machine state that we must be free to reset without touching anything of theirs.
 */
class DataStoreSearchBackfillStore(
    context: Context,
    sessionId: SessionId,
    sessionCoroutineScope: CoroutineScope,
) : SearchBackfillStore {
    companion object {
        fun storeFile(context: Context, sessionId: SessionId): File {
            val hashedUserId = sessionId.value.hash().take(16)
            return context.preferencesDataStoreFile("search_backfill_$hashedUserId")
        }
    }

    private val cursorKey = stringPreferencesKey("cursor")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataStoreFile = storeFile(context, sessionId)
    private val store = PreferenceDataStoreFactory.create(scope = sessionCoroutineScope) { dataStoreFile }

    override fun cursorFlow(): Flow<SearchBackfillCursor?> = store.data.map { preferences ->
        preferences[cursorKey]?.let(::decode)
    }

    override suspend fun getCursor(): SearchBackfillCursor? = cursorFlow().first()

    override suspend fun setCursor(cursor: SearchBackfillCursor) {
        store.edit { preferences ->
            preferences[cursorKey] = json.encodeToString(cursor)
        }
    }

    override suspend fun clear() {
        store.edit { it.remove(cursorKey) }
    }

    /**
     * A cursor we cannot read is treated as absent, not as an error: the sweep then starts a fresh
     * generation. Losing progress is cheap; crashing a background worker on a schema change is not.
     */
    private fun decode(raw: String): SearchBackfillCursor? = try {
        json.decodeFromString<SearchBackfillCursor>(raw)
    } catch (error: IllegalArgumentException) {
        Timber.w(error, "Discarding unreadable search backfill cursor")
        null
    }
}
