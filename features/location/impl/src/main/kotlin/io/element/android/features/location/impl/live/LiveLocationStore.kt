/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.time.Instant

private const val LIVE_LOCATION_EXPIRY_VALUE_SEPARATOR = "="

@Inject
@SingleIn(SessionScope::class)
class LiveLocationStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
    sessionId: SessionId,
) {
    private val store = preferenceDataStoreFactory.create("location_${sessionId.value.hash().take(16)}")
    private val acceptedLiveLocationDisclaimerKey = booleanPreferencesKey("live_location_disclaimer_accepted")
    private val liveLocationExpiriesKey = stringSetPreferencesKey("live_location_expiries")

    suspend fun hasAcceptedLiveLocationDisclaimer(): Boolean = runCatchingExceptions {
        store.data.first()[acceptedLiveLocationDisclaimerKey] ?: false
    }.getOrDefault(false)

    suspend fun setAcceptedLiveLocationDisclaimer(): Result<Unit> = runCatchingExceptions {
        store.edit { prefs ->
            prefs[acceptedLiveLocationDisclaimerKey] = true
        }
    }

    suspend fun getLiveLocationExpiries(): Map<RoomId, Instant> = runCatchingExceptions {
        val serialized = store.data.first()[liveLocationExpiriesKey].orEmpty()
        decodeLiveLocationExpiries(serialized)
    }.onFailure { error ->
        Timber.e(error, "Failed to decode live location expiry payload")
    }.getOrDefault(emptyMap())

    suspend fun setLiveLocationExpiry(roomId: RoomId, expiresAt: Instant): Result<Unit> = runCatchingExceptions {
        store.edit { prefs ->
            val current = decodeLiveLocationExpiries(prefs[liveLocationExpiriesKey].orEmpty())
            prefs[liveLocationExpiriesKey] = encodeLiveLocationExpiries(current + (roomId to expiresAt))
        }
    }

    suspend fun removeLiveLocationExpiry(roomId: RoomId): Result<Unit> = runCatchingExceptions {
        store.edit { prefs ->
            val current = decodeLiveLocationExpiries(prefs[liveLocationExpiriesKey].orEmpty())
            val updated = current - roomId
            if (updated.isEmpty()) {
                prefs.remove(liveLocationExpiriesKey)
            } else {
                prefs[liveLocationExpiriesKey] = encodeLiveLocationExpiries(updated)
            }
        }
    }

    private fun decodeLiveLocationExpiries(serialized: Set<String>): Map<RoomId, Instant> {
        return runCatchingExceptions {
            serialized
                .map { it.split(LIVE_LOCATION_EXPIRY_VALUE_SEPARATOR) }
                .associate { values ->
                    val roomId = RoomId(values[0])
                    val expiresAtMillis = values[1].toLong()
                    roomId to Instant.fromEpochMilliseconds(expiresAtMillis)
                }
        }.getOrDefault(emptyMap())
    }

    private fun encodeLiveLocationExpiries(expiries: Map<RoomId, Instant>): Set<String> {
        return expiries.entries.map { (roomId, expiresAt) ->
            "${roomId.value}$LIVE_LOCATION_EXPIRY_VALUE_SEPARATOR${expiresAt.toEpochMilliseconds()}"
        }.toSet()
    }

    suspend fun clear() {
        store.edit { prefs -> prefs.clear() }
    }
}
