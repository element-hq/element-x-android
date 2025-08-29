/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

interface RecentEmojisProvider {
    suspend fun add(emoji: String): Result<Unit>
    suspend fun reset()
    fun getAllFlow(): Flow<List<String>>
}

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
class RustRecentEmojisProvider @Inject constructor(
    private val matrixClient: MatrixClient,
) : RecentEmojisProvider {
    private val recentEmojisFlow = MutableSharedFlow<List<String>>(replay = 1)

    override suspend fun add(emoji: String): Result<Unit> {
        return runCatchingExceptions {
            matrixClient.addRecentlyUsedEmoji(emoji)

            // Wait a bit and reload the recently used emojis
            delay(500.milliseconds)
            matrixClient.getRecentlyUsedEmojis().onSuccess {
                recentEmojisFlow.emit(it)
            }
        }
    }

    override suspend fun reset() {
        // TODO: nothing
    }

    override fun getAllFlow(): Flow<List<String>> {
        return recentEmojisFlow.onStart {
            matrixClient.getRecentlyUsedEmojis().onSuccess {
                emit(it)
            }
        }
    }

}

class RecentEmojisPreferenceStore @Inject constructor(
    currentSessionIdHolder: CurrentSessionIdHolder,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : RecentEmojisProvider {
    private val store = preferenceDataStoreFactory.create("${currentSessionIdHolder.current.value.hash()}_emoji_history")
    private val emojiPreferenceKey = stringPreferencesKey("emoji_list")

    override suspend fun add(emoji: String): Result<Unit> {
        var result: Result<Unit> = Result.success(Unit)
        store.edit { preferences ->
            val existingEmojis = preferences.loadAndParseList().toMutableList()

            existingEmojis.remove(emoji)
            existingEmojis.add(0, emoji) // Add to the front
            existingEmojis.take(60)

            result = runCatchingExceptions { Json.encodeToString(existingEmojis) }
                .onSuccess {
                    preferences[emojiPreferenceKey] = it
                }
                .map {}
        }

        return result
    }

    override suspend fun reset() {
        store.edit { preferences -> preferences.remove(emojiPreferenceKey) }
    }

    override fun getAllFlow() = store.data.map { it.loadAndParseList() }

    private fun Preferences.loadAndParseList(): List<String> {
        val jsonString = this[emojiPreferenceKey] ?: return emptyList()
        return runCatchingExceptions {
            Json.decodeFromString<List<String>>(jsonString)
        }.getOrNull() ?: emptyList()
    }
}
