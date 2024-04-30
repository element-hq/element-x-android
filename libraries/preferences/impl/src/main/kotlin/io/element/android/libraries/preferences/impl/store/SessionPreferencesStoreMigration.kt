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

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

class SessionPreferencesStoreMigration(
    private val sharePresenceKey: Preferences.Key<Boolean>,
    private val sendPublicReadReceiptsKey: Preferences.Key<Boolean>,
) : DataMigration<Preferences> {
    override suspend fun cleanUp() = Unit

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return currentData[sharePresenceKey] == null
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        // If sendPublicReadReceiptsKey was false, consider that sharing presence is false.
        val defaultValue = currentData[sendPublicReadReceiptsKey] ?: true
        return currentData.toMutablePreferences().apply {
            set(sharePresenceKey, defaultValue)
        }.toPreferences()
    }
}
