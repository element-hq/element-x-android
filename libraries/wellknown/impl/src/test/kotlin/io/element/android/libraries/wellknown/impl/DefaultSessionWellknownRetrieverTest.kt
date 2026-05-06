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
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.api.CacheStore
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.sessionstorage.test.InMemoryCacheStore
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSessionWellknownRetrieverTest {
    @Test
    fun `get empty element wellknown`() = runTest {
        val getUrlLambda = lambdaRecorder<String, Result<ByteArray>> {
            Result.success("{}".toByteArray())
        }
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = getUrlLambda,
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = null,
                    enforceElementPro = null,
                    rageshakeUrl = null,
                    brandColor = null,
                    notificationSound = null,
                    identityProviderAppScheme = null,
                )
            )
        )
        getUrlLambda.assertions().isCalledOnce()
            .with(value("https://user.domain.org/.well-known/element/element.json"))
    }

    @Test
    fun `get element wellknown with full content`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    WELLKNOWN_CONTENT.toByteArray()
                )
            }
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = "#FF0000",
                    notificationSound = "a_notification_sound.flac",
                    identityProviderAppScheme = "an_app_scheme",
                )
            )
        )
    }

    @Test
    fun `get element wellknown with unknown key`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "registration_helper_url": "a_registration_url",
                    "enforce_element_pro": true,
                    "rageshake_url": "a_rageshake_url",
                    // Note the trailing comma, and the comment!
                    "other": true,
                }""".trimIndent().toByteArray()
                )
            },
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = null,
                    notificationSound = null,
                    identityProviderAppScheme = null,
                )
            )
        )
    }

    @Test
    fun `get element wellknown json error`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "registration_helper_url" = "a_registration_url",
                    error
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getElementWellKnown()).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.failure(AN_EXCEPTION)
            }
        )
        assertThat(sut.getElementWellKnown()).isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown hitting cache`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = { lambdaError() },
            cacheStore = InMemoryCacheStore(
                initialData = mapOf(
                    WELLKNOWN_URL to CacheData(
                        value = WELLKNOWN_CONTENT,
                        updatedAt = A_FAKE_TIMESTAMP,
                    )
                )
            )
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = "#FF0000",
                    notificationSound = "a_notification_sound.flac",
                    identityProviderAppScheme = "an_app_scheme",
                )
            )
        )
    }

    @Test
    fun `get element wellknown hitting cache containing invalid json`() = runTest {
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf(
                WELLKNOWN_URL to CacheData(
                    value = WELLKNOWN_CONTENT,
                    updatedAt = A_FAKE_TIMESTAMP,
                )
            )
        )
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success("{}".toByteArray())
            },
            cacheStore = cacheStore,
            jsonProvider = JsonProvider { error("Failed to parse JSON") }
        )
        assertThat(sut.getElementWellKnown()).isInstanceOf(WellknownRetrieverResult.Error::class.java)
        // Ensure that the cache is deleted after the failure to parse it
        assertThat(cacheStore.dataMap).isEmpty()
    }

    @Test
    fun `get element wellknown hitting outdated cache`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success("{}".toByteArray())
            },
            cacheStore = InMemoryCacheStore(
                initialData = mapOf(
                    WELLKNOWN_URL to CacheData(
                        value = WELLKNOWN_CONTENT,
                        updatedAt = 0L,
                    )
                ),
            ),
            // 3 days later, so the cache is outdated
            systemClock = FakeSystemClock(3 * 24 * 60 * 60 * 1000L)
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                ElementWellKnown(
                    registrationHelperUrl = "a_registration_url",
                    enforceElementPro = true,
                    rageshakeUrl = "a_rageshake_url",
                    brandColor = "#FF0000",
                    notificationSound = "a_notification_sound.flac",
                    identityProviderAppScheme = "an_app_scheme",
                )
            )
        )
        // Next call returns the updated value
        runCurrent()
        assertThat(sut.getElementWellKnown()).isEqualTo(
            WellknownRetrieverResult.Success(
                anElementWellKnown()
            )
        )
    }

    private fun TestScope.createDefaultSessionWellknownRetriever(
        getUrlLambda: (String) -> Result<ByteArray>,
        jsonProvider: JsonProvider = DefaultJsonProvider(),
        cacheStore: CacheStore = InMemoryCacheStore(),
        systemClock: SystemClock = FakeSystemClock(),
    ) = DefaultSessionWellknownRetriever(
        matrixClient = FakeMatrixClient(
            userIdServerNameLambda = { "user.domain.org" },
            getUrlLambda = getUrlLambda,
        ),
        json = jsonProvider,
        cacheStore = cacheStore,
        systemClock = systemClock,
        sessionCoroutineScope = backgroundScope,
    )

    companion object {
        private const val WELLKNOWN_URL = "https://user.domain.org/.well-known/element/element.json"
        private const val WELLKNOWN_CONTENT = """{
                "registration_helper_url": "a_registration_url",
                "enforce_element_pro": true,
                "rageshake_url": "a_rageshake_url",
                "brand_color": "#FF0000",
                "notification_sound": "a_notification_sound.flac",
                "idp_app_scheme": "an_app_scheme"
            }"""
    }
}
