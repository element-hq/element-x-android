/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.wellknown.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.wellknown.test.FakeElementWellknownStore
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphrase
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultWellknownRetrieverTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.close()
    }

    @Test
    fun `get empty element wellknown`() = runTest {
        mockWebServer.enqueue(MockResponse(body = "{}"))

        val clientSpy = spyk(OkHttpClient())
        val sut = createDefaultWellknownRetriever(
            callFactory = { clientSpy.newCall(it) }
        )
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = null,
                    enforceElementPro = null,
                    rageshakeUrl = null,
                    brandColor = null,
                    notificationSound = null,
                    identityProviderAppScheme = null,
                    customRecoveryPassphrase = null,
                    contentScannerUrl = null,
                )
            )
        )

        verify(exactly = 1) { clientSpy.newCall(any()) }
    }

    @Test
    fun `get element wellknown with full content`() = runTest {
        mockWebServer.enqueue(MockResponse(body = WELLKNOWN_CONTENT))

        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = "#FF0000",
                    notificationSound = "a_notification_sound.flac",
                    identityProviderAppScheme = "an_app_scheme",
                    customRecoveryPassphrase = null,
                    contentScannerUrl = "https://content-scanner.example.com",
                )
            )
        )
    }

    @Test
    fun `get element wellknown with unknown key`() = runTest {
        mockWebServer.enqueue(
            MockResponse(
                body = """{
            "registration_helper_url": "a_registration_url",
            "enforce_element_pro": true,
            "rageshake_url": "a_rageshake_url",
            "unknown_key": "unknown_value"
        }""".trimIndent()
            )
        )
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = null,
                    notificationSound = null,
                    identityProviderAppScheme = null,
                    contentScannerUrl = null,
                    customRecoveryPassphrase = null,
                )
            )
        )
    }

    @Test
    fun `get element wellknown with custom recovery passphrase settings`() = runTest {
        mockWebServer.enqueue(
            MockResponse(
                body = """{
                    "custom_recovery_passphrase": {
                        "min_character_count": 8
                    }
                }""".trimIndent()
            )
        )
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 8)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with custom recovery passphrase settings missing min character count floors to 1`() = runTest {
        mockWebServer.enqueue(
            MockResponse(
                body = """{
                    "custom_recovery_passphrase": {}
                }""".trimIndent()
            )
        )
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with zero min character count floors to 1`() = runTest {
        mockWebServer.enqueue(
            MockResponse(
                body = """{
            "custom_recovery_passphrase": {
                "min_character_count": 0
            }
        }""".trimIndent()
            )
        )
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with negative min character count floors to 1`() = runTest {
        mockWebServer.enqueue(
            MockResponse(
                body = """{
            "custom_recovery_passphrase": {
                "min_character_count": -5
            }
        }""".trimIndent()
            )
        )
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown json error`() = runTest {
        mockWebServer.enqueue(MockResponse(code = 500))
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        mockWebServer.enqueue(MockResponse(code = 401))
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown 404 http error counts as not found`() = runTest {
        mockWebServer.enqueue(MockResponse(code = 404))
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isInstanceOf(WellknownRetrieverResult.NotFound::class.java)
    }

    @Test
    fun `get element wellknown hitting cache containing invalid json`() = runTest {
        mockWebServer.enqueue(MockResponse(body = "invalid json"))
        val cacheStore = FakeElementWellknownStore(
            initialData = mapOf(
                wellKnownUrl(mockWebServer) to WellknownRetrieverResult.Error(IllegalStateException("Invalid JSON"))
            )
        )
        val sut = createDefaultWellknownRetriever(
            cacheStore = cacheStore,
            jsonProvider = JsonProvider { error("Failed to parse JSON") }
        )
        assertThat(sut.getElementWellKnown(wellKnownUrl(mockWebServer))).isInstanceOf(WellknownRetrieverResult.Error::class.java)
        // Ensure that the cache is deleted after the failure to parse it
        assertThat(cacheStore.get(wellKnownUrl(mockWebServer))).isEqualTo(WellknownRetrieverResult.NotFound)
    }

    private fun createDefaultWellknownRetriever(
        callFactory: Call.Factory = Call.Factory { request: Request -> OkHttpClient().newCall(request) },
        cacheStore: FakeElementWellknownStore = FakeElementWellknownStore(),
        jsonProvider: JsonProvider = DefaultJsonProvider(),
    ) = DefaultWellknownRetriever(
        retrofitFactory = RetrofitFactory(
            callFactory = { callFactory },
            json = { jsonProvider }
        ),
        jsonProvider = jsonProvider,
        elementWellknownStore = cacheStore,
    )
}

private const val WELLKNOWN_CONTENT = """{
                "registration_helper_url": "a_registration_url",
                "enforce_element_pro": true,
                "rageshake_url": "a_rageshake_url",
                "brand_color": "#FF0000",
                "notification_sound": "a_notification_sound.flac",
                "idp_app_scheme": "an_app_scheme",
                "content_scanner_url": "https://content-scanner.example.com"
            }"""

private fun wellKnownUrl(server: MockWebServer): String {
    return "http://${server.hostName}:${server.port}/.well-known/element/element.json"
}
