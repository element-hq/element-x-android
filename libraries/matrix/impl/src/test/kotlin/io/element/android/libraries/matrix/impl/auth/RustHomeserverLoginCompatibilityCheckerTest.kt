/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.FakeClientBuilderProvider
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverLoginDetails
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test

@Ignore("JNA direct mapping has broken unit tests with FFI fakes")
class RustHomeserverLoginCompatibilityCheckerTest {
    @Test
    fun `check - is valid if it supports OIDC login`() = runTest {
        val sut = createChecker { FakeFfiHomeserverLoginDetails(supportsOidcLogin = true) }
        assertThat(sut.check("https://matrix.host.org").getOrNull()).isTrue()
    }

    @Test
    fun `check - is valid if it supports password login`() = runTest {
        val sut = createChecker { FakeFfiHomeserverLoginDetails(supportsPasswordLogin = true) }
        assertThat(sut.check("https://matrix.host.org").getOrNull()).isTrue()
    }

    @Test
    fun `check - is not valid if it only supports SSO login`() = runTest {
        val sut = createChecker { FakeFfiHomeserverLoginDetails(supportsSsoLogin = true) }
        assertThat(sut.check("https://matrix.host.org").getOrNull()).isFalse()
    }

    @Test
    fun `check - is not valid if fetching the data fails`() = runTest {
        val sut = createChecker { error("Unexpected error!") }
        assertThat(sut.check("https://matrix.host.org").isFailure).isTrue()
    }

    private fun createChecker(
        result: () -> FakeFfiHomeserverLoginDetails,
    ) = RustHomeServerLoginCompatibilityChecker(
        clientBuilderProvider = FakeClientBuilderProvider {
            FakeFfiClientBuilder {
                FakeFfiClient(homeserverLoginDetailsResult = result)
            }
        }
    )
}
