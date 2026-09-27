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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.Test
import java.net.PortUnreachableException

class DefaultWellknownRetrieverTest {
    @Test
    fun `get empty element wellknown`() = runTest {
        val mockCall = mockCall(defaultResponse(body = "{}"))
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall },
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
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
        verify(exactly = 1) { mockCall.enqueue(any()) }
    }

    @Test
    fun `get element wellknown with full content`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall(defaultResponse()) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
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
        val sut = createDefaultWellknownRetriever(
            callFactory = {
                mockCall(defaultResponse(body = """{
                    "registration_helper_url": "a_registration_url",
                    "enforce_element_pro": true,
                    "rageshake_url": "a_rageshake_url",
                    // Note the trailing comma, and the comment!
                    "other": true,
                }""".trimIndent()))
            }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
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
        val sut = createDefaultWellknownRetriever(
            callFactory = {
                mockCall(defaultResponse(body = """{
                    "custom_recovery_passphrase": {
                        "min_character_count": 8
                    }
                }""".trimIndent()))
            }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 8)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with custom recovery passphrase settings missing min character count floors to 1`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = {
                mockCall(defaultResponse(body = """{
                    "custom_recovery_passphrase": {}
                }""".trimIndent()))
            }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with zero min character count floors to 1`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = {
                mockCall(
                    defaultResponse(
                        body = """{
                            "custom_recovery_passphrase": {
                                "min_character_count": 0
                            }
                        }""".trimIndent()
                    )
                )
            }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown with negative min character count floors to 1`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = {
                mockCall(
                    defaultResponse(
                        body = """{
                            "custom_recovery_passphrase": {
                                "min_character_count": -5
                            }
                        }""".trimIndent()
                    )
                )
            }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown(
                    customRecoveryPassphrase = CustomRecoveryPassphrase(minCharacterCount = 1)
                )
            )
        )
    }

    @Test
    fun `get element wellknown json error`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall(defaultResponse(status = 500)) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall(error = PortUnreachableException()) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown 404 http error counts as not found`() = runTest {
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall(defaultResponse(status = 404)) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isInstanceOf(WellknownRetrieverResult.NotFound::class.java)
    }

    @Test
    fun `get element wellknown hitting cache containing invalid json`() = runTest {
        val cacheStore = FakeElementWellknownStore(
            initialData = mapOf(
                WELLKNOWN_URL to WellknownRetrieverResult.Error(IllegalStateException("Invalid JSON"))
            )
        )
        val sut = createDefaultWellknownRetriever(
            callFactory = { mockCall(defaultResponse(body = "invalid json")) },
            cacheStore = cacheStore,
            jsonProvider = JsonProvider { error("Failed to parse JSON") }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL)).isInstanceOf(WellknownRetrieverResult.Error::class.java)
        // Ensure that the cache is deleted after the failure to parse it
        assertThat(cacheStore.get(WELLKNOWN_URL)).isEqualTo(WellknownRetrieverResult.NotFound)
    }

    private fun defaultResponse(
        body: String = WELLKNOWN_CONTENT,
        status: Int = 200,
    ) = Response.Builder()
        .code(status)
        .message("OK")
        .protocol(okhttp3.Protocol.HTTP_1_1)
        .request(Request.Builder().url(WELLKNOWN_URL).build())
        .body(body.toByteArray().toResponseBody())
        .build()

    private fun createDefaultWellknownRetriever(
        callFactory: Call.Factory = Call.Factory { _: Request -> mockCall(defaultResponse()) },
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

    companion object {
        private const val WELLKNOWN_URL = "https://user.domain.org/.well-known/element/element.json"
        private const val WELLKNOWN_CONTENT = """{
                "registration_helper_url": "a_registration_url",
                "enforce_element_pro": true,
                "rageshake_url": "a_rageshake_url",
                "brand_color": "#FF0000",
                "notification_sound": "a_notification_sound.flac",
                "idp_app_scheme": "an_app_scheme",
                "content_scanner_url": "https://content-scanner.example.com"
            }"""
    }
}

private fun mockCall(
    response: Response? = null,
    error: IOException? = null,
): Call = mockk<Call> {
    if (error != null) {
        every { enqueue(any()) } answers {
            val callback = firstArg<okhttp3.Callback>()
            callback.onFailure(this@mockk, error)
        }
    } else if (response != null) {
        every { enqueue(any()) } answers {
            val callback = firstArg<okhttp3.Callback>()
            callback.onResponse(this@mockk, response)
        }
    } else {
        error("Either response or error must be provided")
    }
}
