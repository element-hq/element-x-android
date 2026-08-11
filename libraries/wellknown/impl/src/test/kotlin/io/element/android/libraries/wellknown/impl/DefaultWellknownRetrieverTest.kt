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
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownParser
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.EnterpriseRemoteConfigSource
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.URL

class DefaultWellknownRetrieverTest {
    @Test
    fun `get element wellknown calls resolver and parser`() = runTest {
        val parser = lambdaRecorder<String, Result<ElementWellKnown>> { Result.success(anElementWellKnown()) }
        val resolver = SpyUrlContentFetcher { Result.success(WELLKNOWN_CONTENT.toByteArray()) }
        val sut = createDefaultWellknownRetriever(
            elementWellKnownParser = parser,
            urlResolver = resolver,
        )

        sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT)

        resolver.assertions().isCalledOnce().with(value(WELLKNOWN_URL))
        parser.assertions().isCalledOnce().with(value(WELLKNOWN_CONTENT))
    }

    @Test
    fun `get element wellknown parser error`() = runTest {
        val sut = createDefaultWellknownRetriever(
            elementWellKnownParser = { Result.failure(IllegalStateException("Invalid JSON")) },
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown network error`() = runTest {
        val resolver = SpyUrlContentFetcher { Result.failure(IllegalStateException("Network error")) }
        val sut = createDefaultWellknownRetriever(urlResolver = resolver)
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown 404 http error counts as not found`() = runTest {
        val resolver = SpyUrlContentFetcher { Result.failure(ClientException.Generic("Http error: 404", null)) }
        val sut = createDefaultWellknownRetriever(urlResolver = resolver)
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.NotFound::class.java)
    }

    @Test
    fun `get element wellknown will return an error if the provided host is not valid`() = runTest {
        val sut = createDefaultWellknownRetriever()
        assertThat(sut.getElementWellKnown("!not valid", EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown will return a cached value if present instead of fetching`() = runTest {
        val host = URL(WELLKNOWN_URL).host.ensureProtocol()
        val sut = createDefaultWellknownRetriever(
            cacheStoreFactory = { FakeElementWellknownStore(initialData = mapOf(host to WellknownRetrieverResult.Success(expectedElementWellKnown))) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Success::class.java)
    }

    @Test
    fun `get element wellknown will return an outdated value first then fetch the current one`() = runTest {
        val host = URL(WELLKNOWN_URL).host.ensureProtocol()
        val resolver = SpyUrlContentFetcher { Result.success(WELLKNOWN_CONTENT.toByteArray()) }
        val sut = createDefaultWellknownRetriever(
            urlResolver = resolver,
            cacheStoreFactory = { FakeElementWellknownStore(initialData = mapOf(host to WellknownRetrieverResult.Outdated(expectedElementWellKnown))) }
        )
        // The first call returns the outdated value
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Outdated::class.java)
        // The resolver is not called yet because the fetch is done in the background
        resolver.assertions().isNeverCalled()

        // We give it some time to run the background fetch
        runCurrent()

        // The resolver is called during the background fetch
        resolver.assertions().isCalledOnce().with(value(WELLKNOWN_URL))
    }

    @Test
    fun `get element wellknown will return an error if it happens when trying to retrieve the cached value`() = runTest {
        val host = URL(WELLKNOWN_URL).host.ensureProtocol()
        val sut = createDefaultWellknownRetriever(
            cacheStoreFactory = { FakeElementWellknownStore(initialData = mapOf(host to WellknownRetrieverResult.Error(IllegalStateException("BOOM")))) }
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `get element wellknown will return an error if it fails to serialize the retrieved value into the cache`() = runTest {
        val sut = createDefaultWellknownRetriever(
            elementWellKnownParser = { Result.failure(IllegalStateException("BOOM")) },
        )
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT))
            .isInstanceOf(WellknownRetrieverResult.Error::class.java)
    }

    @Test
    fun `WELLKNOWN_ENDPOINT source creates a store with no prefix`() = runTest {
        val storeFactory = lambdaRecorder<String?, ElementWellknownStore> { FakeElementWellknownStore() }
        val sut = createDefaultWellknownRetriever(cacheStoreFactory = storeFactory)

        sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT)

        storeFactory.assertions().isCalledOnce().with(value(null))
    }

    @Test
    fun `ESS_CONFIG source creates a store with ess prefix and checks the ESS config endpoint`() = runTest {
        val essConfigUrl = "https://ess-config.example.com"
        val storeFactory = lambdaRecorder<String?, ElementWellknownStore> { FakeElementWellknownStore() }
        val resolver = SpyUrlContentFetcher { Result.success(WELLKNOWN_CONTENT.toByteArray()) }
        val sut = createDefaultWellknownRetriever(
            cacheStoreFactory = storeFactory,
            enterpriseService = FakeEnterpriseService(
                overrideWellKnownResult = { null },
                essConfigEndpointUrlResult = { essConfigUrl },
            ),
            urlResolver = resolver,
        )

        sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.ESS_CONFIG)

        storeFactory.assertions().isCalledOnce().with(value("ess_config"))
        resolver.assertions().isCalledOnce().with(value(essConfigUrl))
    }

    @Test
    fun `get element wellknown was overridden`() = runTest {
        val wellKnown = anElementWellKnown()

        val resolver = SpyUrlContentFetcher { Result.success(WELLKNOWN_CONTENT.toByteArray()) }

        val sut = createDefaultWellknownRetriever(
            enterpriseService = FakeEnterpriseService(
                overrideWellKnownResult = { wellKnown },
            ),
            urlResolver = resolver,
        )

        // The overridden value is returned
        assertThat(sut.getElementWellKnown(WELLKNOWN_URL, EnterpriseRemoteConfigSource.WELLKNOWN_ENDPOINT)).isEqualTo(
            WellknownRetrieverResult.Success(wellKnown)
        )

        // And the endpoint is never hit
        resolver.assertions().isNeverCalled()
    }

    private fun TestScope.createDefaultWellknownRetriever(
        cacheStoreFactory: ElementWellknownStore.Factory = { FakeElementWellknownStore() },
        elementWellKnownParser: ElementWellKnownParser = { Result.success(expectedElementWellKnown) },
        enterpriseService: FakeEnterpriseService = FakeEnterpriseService(overrideWellKnownResult = { null }),
        urlResolver: UrlContentFetcher = SpyUrlContentFetcher { Result.success(WELLKNOWN_CONTENT.toByteArray()) },
    ) = DefaultWellknownRetriever(
        elementWellknownStoreFactory = cacheStoreFactory,
        enterpriseService = enterpriseService,
        elementWellKnownParser = elementWellKnownParser,
        urlContentFetcher = urlResolver,
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

private class SpyUrlContentFetcher(lambda: (String) -> Result<ByteArray>) : UrlContentFetcher {
    private val spy = lambdaRecorder<String, Result<ByteArray>> { lambda(it) }

    override suspend fun getUrl(url: String): Result<ByteArray> {
        return spy(url)
    }

    fun assertions() = spy.assertions()
}
