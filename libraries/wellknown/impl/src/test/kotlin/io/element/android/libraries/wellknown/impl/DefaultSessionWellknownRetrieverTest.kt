/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
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
                    """{
                    "registration_helper_url": "a_registration_url",
                    "enforce_element_pro": true,
                    "rageshake_url": "a_rageshake_url",
                    "brand_color": "#FF0000"
                }""".trimIndent().toByteArray()
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
                    "other": true
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

    private fun createDefaultSessionWellknownRetriever(
        getUrlLambda: (String) -> Result<ByteArray>,
    ) = DefaultSessionWellknownRetriever(
        matrixClient = FakeMatrixClient(
            userIdServerNameLambda = { "user.domain.org" },
            getUrlLambda = getUrlLambda,
        ),
        json = DefaultJsonProvider(),
    )
}
