/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
