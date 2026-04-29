/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first

private val acceptedLiveLocationDisclaimerKey = booleanPreferencesKey("live_location_disclaimer_accepted")

@Inject
@SingleIn(SessionScope::class)
class LiveLocationStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
    sessionId: SessionId,
) {
    private val store = preferenceDataStoreFactory.create("location_${sessionId.value.hash().take(16)}")

    suspend fun hasAcceptedLiveLocationDisclaimer(): Boolean = runCatching {
        store.data.first()[acceptedLiveLocationDisclaimerKey] ?: false
    }.getOrDefault(false)

    suspend fun setAcceptedLiveLocationDisclaimer(): Result<Unit> = runCatching {
        store.edit { prefs ->
            prefs[acceptedLiveLocationDisclaimerKey] = true
        }
    }
}
