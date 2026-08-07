/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.impl.live.LiveLocationStore
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class LiveLocationStoreTest {
    private val preferenceDataStoreFactory = FakePreferenceDataStoreFactory()

    @Test
    fun `disclaimer defaults to false`() = runTest {
        val store = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )

        assertThat(store.hasAcceptedLiveLocationDisclaimer()).isFalse()
    }

    @Test
    fun `disclaimer acceptance is isolated per session`() = runTest {
        val firstStore = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )
        val secondStore = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = SessionId("@other:server"),
        )

        firstStore.setAcceptedLiveLocationDisclaimer().getOrThrow()

        assertThat(firstStore.hasAcceptedLiveLocationDisclaimer()).isTrue()
        assertThat(secondStore.hasAcceptedLiveLocationDisclaimer()).isFalse()
    }

    @Test
    fun `can persist and read expiry per room`() = runTest {
        val store = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )

        store.setLiveLocationExpiry(A_ROOM_ID, Instant.fromEpochMilliseconds(1_000L)).getOrThrow()

        assertThat(store.getLiveLocationExpiries())
            .containsExactly(A_ROOM_ID, Instant.fromEpochMilliseconds(1_000L))
    }

    @Test
    fun `removing one expiry leaves others untouched`() = runTest {
        val otherRoomId = RoomId("!other:server")
        val store = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )

        store.setLiveLocationExpiry(A_ROOM_ID, Instant.fromEpochMilliseconds(1_000L)).getOrThrow()
        store.setLiveLocationExpiry(otherRoomId, Instant.fromEpochMilliseconds(2_000L)).getOrThrow()
        store.removeLiveLocationExpiry(A_ROOM_ID).getOrThrow()

        assertThat(store.getLiveLocationExpiries())
            .containsExactly(otherRoomId, Instant.fromEpochMilliseconds(2_000L))
    }

    @Test
    fun `setting expiry twice replaces the existing room value`() = runTest {
        val store = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )

        store.setLiveLocationExpiry(A_ROOM_ID, Instant.fromEpochMilliseconds(1_000L)).getOrThrow()
        store.setLiveLocationExpiry(A_ROOM_ID, Instant.fromEpochMilliseconds(2_000L)).getOrThrow()

        assertThat(store.getLiveLocationExpiries())
            .containsExactly(A_ROOM_ID, Instant.fromEpochMilliseconds(2_000L))
    }

    @Test
    fun `malformed expiry payload returns empty map`() = runTest {
        val store = LiveLocationStore(
            preferenceDataStoreFactory = createMalformedExpiryPreferenceDataStoreFactory(),
            sessionId = A_SESSION_ID,
        )

        assertThat(store.getLiveLocationExpiries()).isEmpty()
    }

    private fun createMalformedExpiryPreferenceDataStoreFactory(): PreferenceDataStoreFactory {
        return object : PreferenceDataStoreFactory {
            override fun create(name: String): DataStore<Preferences> {
                var preferences: Preferences = mutablePreferencesOf(
                    stringPreferencesKey("live_location_expiries") to "not valid"
                )
                return object : DataStore<Preferences> {
                    override val data: Flow<Preferences>
                        get() = flowOf(preferences)

                    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                        preferences = transform(preferences)
                        return preferences
                    }
                }
            }
        }
    }
}
