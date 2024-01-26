/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultSessionPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
    currentSessionIdHolder: CurrentSessionIdHolder,
) : SessionPreferencesStore {
    private val sendPublicReadReceiptsKey = booleanPreferencesKey("sendPublicReadReceipts")
    private val hashedUserId = currentSessionIdHolder.current.value.hash().take(16)

    private val store = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("session_${currentSessionIdHolder.current}_preferences")
    }

    override suspend fun setSendPublicReadReceipts(enabled: Boolean) = update(sendPublicReadReceiptsKey, enabled)
    override fun isSendPublicReadReceiptsEnabled(): Flow<Boolean> = get(sendPublicReadReceiptsKey, true)

    override suspend fun clear() {
        store.edit { it.clear() }
    }

    private suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        store.edit { prefs -> prefs[key] = value }
    }

    private fun <T> get(key: Preferences.Key<T>, default: T): Flow<T> {
        return store.data.map { prefs -> prefs[key] ?: default }
    }
}
