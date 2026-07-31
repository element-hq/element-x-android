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
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.wellknown.test.FakeElementWellknownStore
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.matrix.api.GetUrlResolver
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownParser
import io.element.android.libraries.wellknown.api.ElementWellKnownSource
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultWellknownRetrieverTest {
    @Test
    fun `get element wellknown calls resolver and parser`() = runTest {
        val parser = lambdaRecorder<String, Result<ElementWellKnown>> { Result.success(anElementWellKnown()) }
        val resolver = SpyGetUrlResolver { Result.success(WELLKNOWN_CONTENT.toByteArray()) }
        val sut = createDefaultWellknownRetriever(
            elementWellKnownParser = parser,
            urlResolver = resolver,
        )

        sut.getElementWellKnown(WELLKNOWN_URL, ElementWellKnownSource.WELLKNOWN_ENDPOINT)

        resolver.assertions().isCalledOnce().with(value(WELLKNOWN_URL))
        parser.assertions().isCalledOnce().with(value(WELLKNOWN_CONTENT))
    }

    @Test
    fun `get element wellknown parser error`() = runTest {
        val sut = createDefaultWellknownRetriever(
            elementWellKnownParser = { Result.failure(IllegalStateException("Invalid JSON")) },
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, ElementWellKnownSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        val resolver = SpyGetUrlResolver { Result.failure(IllegalStateException("Network error")) }
        val sut = createDefaultWellknownRetriever(urlResolver = resolver)
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, ElementWellKnownSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown 404 http error counts as not found`() = runTest {
        val resolver = SpyGetUrlResolver { Result.failure(ClientException.Generic("Http error: 404", null)) }
        val sut = createDefaultWellknownRetriever(urlResolver = resolver)
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, ElementWellKnownSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.NotFound::class.java)
    }

    @Test
    fun `get element wellknown was overridden`() = runTest {
        val wellKnown = anElementWellKnown()

        val resolver = SpyGetUrlResolver { Result.success(WELLKNOWN_CONTENT.toByteArray()) }

        val sut = createDefaultWellknownRetriever(
            enterpriseService = FakeEnterpriseService(
                overrideWellKnownResult = { wellKnown },
            ),
            urlResolver = resolver,
        )

        // The overridden value is returned
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, ElementWellKnownSource.WELLKNOWN_ENDPOINT)).isEqualTo(
            WellknownRetrieverResult.Success(wellKnown)
        )

        // And the endpoint is never hit
        resolver.assertions().isNeverCalled()
    }

    private fun TestScope.createDefaultWellknownRetriever(
        cacheStore: FakeElementWellknownStore = FakeElementWellknownStore(),
        elementWellKnownParser: ElementWellKnownParser = { Result.success(expectedElementWellKnown) },
        enterpriseService: FakeEnterpriseService = FakeEnterpriseService(overrideWellKnownResult = { null }),
        urlResolver: GetUrlResolver = SpyGetUrlResolver { Result.success(WELLKNOWN_CONTENT.toByteArray()) },
    ) = DefaultWellknownRetriever(
        elementWellknownStoreFactory = { cacheStore },
        enterpriseService = enterpriseService,
        elementWellKnownParser = elementWellKnownParser,
        getUrlResolver = urlResolver,
        coroutineScope = backgroundScope,
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
                "content_scanner_url": "https://content-scanner.example.com",
                "force_disable_e2ee": false
            }"""

        private val expectedElementWellKnown = ElementWellKnown(
            registrationHelperUrl = "a_registration_url",
            enforceElementPro = true,
            rageshakeUrl = "a_rageshake_url",
            brandColor = "#FF0000",
            notificationSound = "a_notification_sound.flac",
            identityProviderAppScheme = "an_app_scheme",
            contentScannerUrl = "https://content-scanner.example.com",
            customRecoveryPassphrase = null,
            forceDisableE2EE = false,
        )
    }
}

private class SpyGetUrlResolver(lambda: (String) -> Result<ByteArray>) : GetUrlResolver {
    private val spy = lambdaRecorder<String, Result<ByteArray>> { lambda(it) }

    override suspend fun getUrl(url: String): Result<ByteArray> {
        return spy(url)
    }

    fun assertions() = spy.assertions()
}
