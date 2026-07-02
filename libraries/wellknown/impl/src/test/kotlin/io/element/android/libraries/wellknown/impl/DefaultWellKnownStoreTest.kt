/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.sessionstorage.test.InMemoryCacheStore
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphrase
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val A_DOMAIN = "matrix.example.com"
private const val A_CACHE_KEY = "https://$A_DOMAIN/.well-known/element/element.json"

// 1 day in millis
private const val CACHE_VALIDITY_MILLIS = 1 * 24 * 60 * 60 * 1000L

class DefaultWellKnownStoreTest {
    private val jsonProvider = DefaultJsonProvider()

    @Test
    fun `get returns NotFound when cache is empty`() = runTest {
        val sut = createDefaultWellKnownStore()
        assertThat(sut.get(A_DOMAIN)).isEqualTo(WellknownRetrieverResult.NotFound)
    }

    @Test
    fun `get returns Success when cache has fresh data`() = runTest {
        val wellKnown = InternalElementWellKnown(rageshakeUrl = "https://rageshake.example.com")
        val jsonString = jsonProvider().encodeToString(wellKnown)
        val clock = FakeSystemClock(epochMillisResult = A_FAKE_TIMESTAMP)
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(A_CACHE_KEY to CacheData(jsonString, A_FAKE_TIMESTAMP))
        )
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore, systemClock = clock)

        val result = sut.get(A_DOMAIN)

        assertThat(result).isEqualTo(WellknownRetrieverResult.Success(wellKnown.map()))
    }

    @Test
    fun `get returns Outdated when cached data has expired`() = runTest {
        val wellKnown = InternalElementWellKnown(rageshakeUrl = "https://rageshake.example.com")
        val jsonString = jsonProvider().encodeToString(wellKnown)
        // Store data at timestamp 0, but clock is past the validity window
        val expiredTimestamp = 0L
        val nowTimestamp = expiredTimestamp + CACHE_VALIDITY_MILLIS + 1
        val clock = FakeSystemClock(epochMillisResult = nowTimestamp)
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(A_CACHE_KEY to CacheData(jsonString, expiredTimestamp))
        )
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore, systemClock = clock)

        val result = sut.get(A_DOMAIN)

        assertThat(result).isEqualTo(WellknownRetrieverResult.Outdated(wellKnown.map()))
    }

    @Test
    fun `get returns Error when cached data is invalid JSON`() = runTest {
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(A_CACHE_KEY to CacheData("not valid json at all", A_FAKE_TIMESTAMP))
        )
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore)

        val result = sut.get(A_DOMAIN)

        assertThat(result).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `update stores data and get returns Success`() = runTest {
        val wellKnown = """
            {
                "registration_helper_url": "https://element.io",
                "enforce_element_pro": true,
                "rageshake_url": "https://example.org/rageshake",
                "brand_color": "#FF0000",
                "notification_sound": "ring.flac",
                "idp_app_scheme": "io.element.app",
                "custom_recovery_passphrase": {
                    "min_character_count": 8
                }
            }
        """.trimIndent()
        val clock = FakeSystemClock(epochMillisResult = A_FAKE_TIMESTAMP)
        val cacheStore = InMemoryCacheStore()
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore, systemClock = clock)

        val updateResult = sut.update(A_DOMAIN, wellKnown)
        assertThat(updateResult.isSuccess).isTrue()

        val result = sut.get(A_DOMAIN)
        assertThat(result).isEqualTo(WellknownRetrieverResult.Success(anElementWellKnown(
            registrationHelperUrl = "https://element.io",
            enforceElementPro = true,
            rageshakeUrl = "https://example.org/rageshake",
            brandColor = "#FF0000",
            notificationSound = "ring.flac",
            identityProviderAppScheme = "io.element.app",
            customRecoveryPassphrase = CustomRecoveryPassphrase(8),
        )))
    }

    @Test
    fun `delete removes data and get returns NotFound`() = runTest {
        val wellKnown = InternalElementWellKnown(rageshakeUrl = "https://rageshake.example.com")
        val jsonString = jsonProvider().encodeToString(wellKnown)
        val clock = FakeSystemClock(epochMillisResult = A_FAKE_TIMESTAMP)
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(A_CACHE_KEY to CacheData(jsonString, A_FAKE_TIMESTAMP))
        )
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore, systemClock = clock)

        val deleteResult = sut.delete(A_DOMAIN)
        assertThat(deleteResult.isSuccess).isTrue()

        assertThat(sut.get(A_DOMAIN)).isEqualTo(WellknownRetrieverResult.NotFound)
    }

    @Test
    fun `get returns data at the exact cache validity boundary as Success`() = runTest {
        val wellKnown = InternalElementWellKnown(notificationSound = "ping.flac")
        val jsonString = jsonProvider().encodeToString(wellKnown)
        // Exactly at the boundary: epochMillis == updatedAt + CACHE_VALIDITY_MILLIS → not expired
        val storedAt = 1000L
        val nowAtBoundary = storedAt + CACHE_VALIDITY_MILLIS
        val clock = FakeSystemClock(epochMillisResult = nowAtBoundary)
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(A_CACHE_KEY to CacheData(jsonString, storedAt))
        )
        val sut = createDefaultWellKnownStore(cacheStore = cacheStore, systemClock = clock)

        val result = sut.get(A_DOMAIN)

        assertThat(result).isEqualTo(WellknownRetrieverResult.Success(wellKnown.map()))
    }

    private fun createDefaultWellKnownStore(
        cacheStore: InMemoryCacheStore = InMemoryCacheStore(),
        systemClock: FakeSystemClock = FakeSystemClock(),
    ) = DefaultWellKnownStore(
        cacheStore = cacheStore,
        json = jsonProvider,
        systemClock = systemClock,
    )
}
