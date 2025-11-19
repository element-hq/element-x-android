/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryResponse
import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryUnifiedPush
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

internal val matrixDiscoveryResponse = {
    DiscoveryResponse(
        unifiedpush = DiscoveryUnifiedPush(
            gateway = "matrix"
        )
    )
}

internal val invalidDiscoveryResponse = {
    DiscoveryResponse(
        unifiedpush = DiscoveryUnifiedPush(
            gateway = ""
        )
    )
}

class DefaultUnifiedPushGatewayResolverTest {
    @Test
    fun `when a custom url provide a correct matrix gateway, the custom url is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("https://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("https://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Success("https://custom.url/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url with port provides a correct matrix gateway, the custom url is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("https://custom.url:123")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("https://custom.url:123")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Success("https://custom.url:123/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url with port and path provides a correct matrix gateway, the custom url is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("https://custom.url:123/some/path")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("https://custom.url:123")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Success("https://custom.url:123/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url with http scheme provides a correct matrix gateway, the custom url is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url:123/some/path")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url:123")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Success("http://custom.url:123/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url is not reachable, the custom url is still returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = { throw AN_EXCEPTION }
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Error("http://custom.url/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url is not found (404), NoMatrixGateway is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = {
                throw HttpException(Response.error<Unit>(HttpURLConnection.HTTP_NOT_FOUND, "".toResponseBody()))
            }
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.NoMatrixGateway)
    }

    @Test
    fun `when a custom url is forbidden (403), NoMatrixGateway is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = {
                throw HttpException(Response.error<Unit>(HttpURLConnection.HTTP_FORBIDDEN, "".toResponseBody()))
            }
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.NoMatrixGateway)
    }

    @Test
    fun `when a custom url is not acceptable (406), NoMatrixGateway is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = {
                throw HttpException(Response.error<Unit>(HttpURLConnection.HTTP_NOT_ACCEPTABLE, "".toResponseBody()))
            }
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.NoMatrixGateway)
    }

    @Test
    fun `when a custom url is internal error (500), Error is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = {
                throw HttpException(Response.error<Unit>(HttpURLConnection.HTTP_INTERNAL_ERROR, "".toResponseBody()))
            }
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("http://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("http://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.Error("http://custom.url/_matrix/push/v1/notify"))
    }

    @Test
    fun `when a custom url is invalid, ErrorInvalidUrl is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("invalid")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isNull()
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.ErrorInvalidUrl)
    }

    @Test
    fun `when a custom url provides a invalid matrix gateway, NoMatrixGateway is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = invalidDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("https://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("https://custom.url")
        assertThat(result).isEqualTo(UnifiedPushGatewayResolverResult.NoMatrixGateway)
    }

    private fun TestScope.createDefaultUnifiedPushGatewayResolver(
        unifiedPushApiFactory: UnifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = { DiscoveryResponse() }
        )
    ) = DefaultUnifiedPushGatewayResolver(
        unifiedPushApiFactory = unifiedPushApiFactory,
        coroutineDispatchers = testCoroutineDispatchers()
    )
}
