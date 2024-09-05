/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryResponse
import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryUnifiedPush
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

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
        assertThat(result).isEqualTo("https://custom.url/_matrix/push/v1/notify")
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
        assertThat(result).isEqualTo("https://custom.url:123/_matrix/push/v1/notify")
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
        assertThat(result).isEqualTo("https://custom.url:123/_matrix/push/v1/notify")
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
        assertThat(result).isEqualTo("http://custom.url:123/_matrix/push/v1/notify")
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
        assertThat(result).isEqualTo("http://custom.url/_matrix/push/v1/notify")
    }

    @Test
    fun `when a custom url is invalid, the default url is returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = matrixDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("invalid")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isNull()
        assertThat(result).isEqualTo(UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL)
    }

    @Test
    fun `when a custom url provides a invalid matrix gateway, the custom url is still returned`() = runTest {
        val unifiedPushApiFactory = FakeUnifiedPushApiFactory(
            discoveryResponse = invalidDiscoveryResponse
        )
        val sut = createDefaultUnifiedPushGatewayResolver(
            unifiedPushApiFactory = unifiedPushApiFactory
        )
        val result = sut.getGateway("https://custom.url")
        assertThat(unifiedPushApiFactory.baseUrlParameter).isEqualTo("https://custom.url")
        assertThat(result).isEqualTo("https://custom.url/_matrix/push/v1/notify")
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
