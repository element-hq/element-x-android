/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultUnifiedPushGatewayUrlResolverTest {
    @Test
    fun `resolve ErrorInvalidUrl returns the default gateway`() {
        val sut = createDefaultUnifiedPushGatewayUrlResolver()
        val result = sut.resolve(
            gatewayResult = UnifiedPushGatewayResolverResult.ErrorInvalidUrl,
            instance = "",
        )
        assertThat(result).isEqualTo(A_UNIFIED_PUSH_GATEWAY)
    }

    @Test
    fun `resolve NoMatrixGateway returns the default gateway`() {
        val sut = createDefaultUnifiedPushGatewayUrlResolver()
        val result = sut.resolve(
            gatewayResult = UnifiedPushGatewayResolverResult.NoMatrixGateway,
            instance = "",
        )
        assertThat(result).isEqualTo(A_UNIFIED_PUSH_GATEWAY)
    }

    @Test
    fun `resolve Success returns the url`() {
        val sut = createDefaultUnifiedPushGatewayUrlResolver()
        val result = sut.resolve(
            gatewayResult = UnifiedPushGatewayResolverResult.Success("aUrl"),
            instance = "",
        )
        assertThat(result).isEqualTo("aUrl")
    }

    @Test
    fun `resolve Error returns the current url when available`() {
        val sut = createDefaultUnifiedPushGatewayUrlResolver(
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { instance ->
                    assertThat(instance).isEqualTo("instance")
                    "aCurrentUrl"
                },
            )
        )
        val result = sut.resolve(
            gatewayResult = UnifiedPushGatewayResolverResult.Error("aUrl"),
            instance = "instance",
        )
        assertThat(result).isEqualTo("aCurrentUrl")
    }

    @Test
    fun `resolve Error returns the url if no current url is available`() {
        val sut = createDefaultUnifiedPushGatewayUrlResolver(
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { instance ->
                    assertThat(instance).isEqualTo("instance")
                    null
                },
            )
        )
        val result = sut.resolve(
            gatewayResult = UnifiedPushGatewayResolverResult.Error("aUrl"),
            instance = "instance",
        )
        assertThat(result).isEqualTo("aUrl")
    }

    private fun createDefaultUnifiedPushGatewayUrlResolver(
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        defaultPushGatewayHttpUrlProvider: DefaultPushGatewayHttpUrlProvider = FakeDefaultPushGatewayHttpUrlProvider(),
    ) = DefaultUnifiedPushGatewayUrlResolver(
        unifiedPushStore = unifiedPushStore,
        defaultPushGatewayHttpUrlProvider = defaultPushGatewayHttpUrlProvider,
    )
}
