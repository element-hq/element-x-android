/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverCapabilities
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustHomeserverCapabilitiesProviderTest {
    @Test
    fun `refresh calls client refresh`() = runTest {
        val refreshLambda = lambdaRecorder<Unit> {}
        val provider = createCapabilitiesProvider(
            capabilities = FakeFfiHomeserverCapabilities(refresh = refreshLambda),
        )
        assertThat(provider.refresh().isSuccess).isTrue()
        refreshLambda.assertions().isCalledOnce()
    }

    @Test
    fun `refresh fails when client refresh does`() = runTest {
        val refreshLambda = lambdaRecorder<Unit> { throw IllegalStateException("Failed to refresh capabilities") }
        val provider = createCapabilitiesProvider(
            capabilities = FakeFfiHomeserverCapabilities(refresh = refreshLambda),
        )
        assertThat(provider.refresh().isFailure).isTrue()
        refreshLambda.assertions().isCalledOnce()
    }

    @Test
    fun `canChangeDisplayName returns expected value`() = runTest {
        val provider = createCapabilitiesProvider(
            capabilities = FakeFfiHomeserverCapabilities(canChangeDisplayName = { true }),
        )
        assertThat(provider.canChangeDisplayName().getOrNull()).isTrue()
    }

    @Test
    fun `canChangeAvatarUrl returns expected value`() = runTest {
        val provider = createCapabilitiesProvider(
            capabilities = FakeFfiHomeserverCapabilities(canChangeAvatar = { true }),
        )
        assertThat(provider.canChangeAvatarUrl().getOrNull()).isTrue()
    }

    @Test
    fun `canChangeDisplayName returns failure when client throws`() = runTest {
        val provider = createCapabilitiesProvider(
            capabilities = FakeFfiHomeserverCapabilities(canChangeDisplayName = { throw IllegalStateException("Failed to get display name capability") }),
        )
        assert(provider.canChangeDisplayName().isFailure)
    }

    private fun createCapabilitiesProvider(
        capabilities: FakeFfiHomeserverCapabilities = FakeFfiHomeserverCapabilities(),
    ) = RustHomeserverCapabilitiesProvider(capabilities)
}
