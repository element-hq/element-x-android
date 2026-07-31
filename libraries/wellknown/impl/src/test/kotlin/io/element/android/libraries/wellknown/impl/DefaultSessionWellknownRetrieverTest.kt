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
import io.element.android.features.wellknown.test.FakeWellknownRetriever
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.wellknown.api.ElementWellKnownSource
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSessionWellknownRetrieverTest {
    private val source = ElementWellKnownSource.WELLKNOWN_ENDPOINT

    @Test
    fun `getWellKnown delegates to inner WellknownRetriever`() = runTest {
        val expectedWellKnown = parsedWellKnownContent()
        val getWellknownRetrieverLambda = lambdaRecorder { _: String, _: ElementWellKnownSource ->
            WellknownRetrieverResult.Success(expectedWellKnown)
        }
        val retriever = createDefaultSessionWellknownRetriever(
            getUrlLambda = { Result.success(WELLKNOWN_CONTENT.toByteArray()) },
            wellKnownRetriever = FakeWellknownRetriever(getWellknownRetrieverLambda),
        )

        val result = retriever.getElementWellKnown(source)

        assertThat(result).isInstanceOf(WellknownRetrieverResult.Success::class.java)
        assertThat((result as WellknownRetrieverResult.Success).data).isEqualTo(expectedWellKnown)
        getWellknownRetrieverLambda.assertions().isCalledOnce()
    }

    private fun parsedWellKnownContent() = DefaultJsonProvider().invoke().decodeFromString<InternalElementWellKnown>(WELLKNOWN_CONTENT).map()

    private fun createDefaultSessionWellknownRetriever(
        getUrlLambda: (String) -> Result<ByteArray>,
        wellKnownRetriever: FakeWellknownRetriever = FakeWellknownRetriever(),
    ) = DefaultSessionWellknownRetriever(
        matrixClient = FakeMatrixClient(
            userIdServerNameLambda = { "user.domain.org" },
            getUrlLambda = getUrlLambda,
        ),
        wellknownRetriever = wellKnownRetriever,
    )

    companion object {
        private const val WELLKNOWN_CONTENT = """{
                "registration_helper_url": "a_registration_url",
                "enforce_element_pro": true,
                "rageshake_url": "a_rageshake_url",
                "brand_color": "#FF0000",
                "notification_sound": "a_notification_sound.flac",
                "idp_app_scheme": "an_app_scheme",
                "content_scanner_url": "https://content-scanner.example.com",
                "force_disable_e2ee": false
            }"""
    }
}
