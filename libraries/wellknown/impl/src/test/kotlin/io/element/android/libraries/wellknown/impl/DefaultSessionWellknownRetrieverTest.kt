/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellKnownBaseConfig
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class DefaultSessionWellknownRetrieverTest {
    @Test
    fun `get empty wellknown`() = runTest {
        val getUrlLambda = lambdaRecorder<String, Result<ByteArray>> {
            Result.success("{}".toByteArray())
        }
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = getUrlLambda,
        )
        assertThat(sut.getWellKnown()).isEqualTo(
            WellKnown(
                homeServer = null,
                identityServer = null,
            )
        )
        getUrlLambda.assertions().isCalledOnce()
            .with(value("https://user.domain.org/.well-known/matrix/client"))
    }

    @Test
    fun `get wellknown with full content`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "m.homeserver": {
                        "base_url": "https://example.org"
                    },
                    "m.identity_server": {
                        "base_url": "https://identity.example.org"
                    }
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getWellKnown()).isEqualTo(
            WellKnown(
                homeServer = WellKnownBaseConfig(
                    baseURL = "https://example.org",
                ),
                identityServer = WellKnownBaseConfig(
                    baseURL = "https://identity.example.org",
                ),
            )
        )
    }

    @Test
    fun `get wellknown with full content empty base_url`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "m.homeserver": {
                        "base_url": "https://example.org"
                    },
                    "m.identity_server": {}
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getWellKnown()).isEqualTo(
            WellKnown(
                homeServer = WellKnownBaseConfig(
                    baseURL = "https://example.org",
                ),
                identityServer = WellKnownBaseConfig(
                    baseURL = null,
                ),
            )
        )
    }

    @Test
    fun `get wellknown with unknown key`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "m.homeserver": {
                        "base_url": "https://example.org"
                    },
                    "m.identity_server": {
                        "base_url": "https://identity.example.org"
                    },
                    "other": true
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getWellKnown()).isEqualTo(
            WellKnown(
                homeServer = WellKnownBaseConfig(
                    baseURL = "https://example.org",
                ),
                identityServer = WellKnownBaseConfig(
                    baseURL = "https://identity.example.org",
                ),
            )
        )
    }

    @Test
    fun `get wellknown json error`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.success(
                    """{
                    "m.homeserver": {
                        "base_url": "https://example.org"
                    },
                    error
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getWellKnown()).isNull()
    }

    @Test
    fun `get wellknown network error`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.failure(AN_EXCEPTION)
            }
        )
        assertThat(sut.getWellKnown()).isNull()
    }

    @Test
    fun `get empty element wellknown`() = runTest {
        val getUrlLambda = lambdaRecorder<String, Result<ByteArray>> {
            Result.success("{}".toByteArray())
        }
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = getUrlLambda,
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            ElementWellKnown(
                registrationHelperUrl = null,
                enforceElementPro = null,
                rageshakeUrl = null,
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
                    """{
                    "registration_helper_url": "a_registration_url",
                    "enforce_element_pro": true,
                    "rageshake_url": "a_rageshake_url"
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            ElementWellKnown(
                registrationHelperUrl = "a_registration_url",
                enforceElementPro = true,
                rageshakeUrl = "a_rageshake_url",
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
                    "other": true
                }""".trimIndent().toByteArray()
                )
            }
        )
        assertThat(sut.getElementWellKnown()).isEqualTo(
            ElementWellKnown(
                registrationHelperUrl = "a_registration_url",
                enforceElementPro = true,
                rageshakeUrl = "a_rageshake_url",
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
        assertThat(sut.getElementWellKnown()).isNull()
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        val sut = createDefaultSessionWellknownRetriever(
            getUrlLambda = {
                Result.failure(AN_EXCEPTION)
            }
        )
        assertThat(sut.getElementWellKnown()).isNull()
    }

    private fun createDefaultSessionWellknownRetriever(
        getUrlLambda: (String) -> Result<ByteArray>,
    ) = DefaultSessionWellknownRetriever(
        matrixClient = FakeMatrixClient(
            userIdServerNameLambda = { "user.domain.org" },
            getUrlLambda = getUrlLambda,
        ),
        parser = Json { ignoreUnknownKeys = true }
    )
}
