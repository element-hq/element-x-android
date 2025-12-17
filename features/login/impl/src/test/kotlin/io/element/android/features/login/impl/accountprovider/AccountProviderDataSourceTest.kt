/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountProviderDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService())
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                AccountProvider(
                    url = AuthenticationConfig.MATRIX_ORG_URL,
                    title = "matrix.org",
                    subtitle = null,
                    isPublic = true,
                    isMatrixOrg = true,
                )
            )
        }
    }

    @Test
    fun `present - initial state - matrix org`() = runTest {
        val sut = AccountProviderDataSource(
            FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(AuthenticationConfig.MATRIX_ORG_URL) }
            )
        )
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                AccountProvider(
                    url = AuthenticationConfig.MATRIX_ORG_URL,
                    title = "matrix.org",
                    subtitle = null,
                    isPublic = true,
                    isMatrixOrg = true,
                )
            )
        }
    }

    @Test
    fun `present - ensure that default homeserver is not star char`() = runTest {
        val sut = AccountProviderDataSource(
            FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(EnterpriseService.ANY_ACCOUNT_PROVIDER, AuthenticationConfig.MATRIX_ORG_URL) }
            )
        )
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                AccountProvider(
                    url = AuthenticationConfig.MATRIX_ORG_URL,
                    title = "matrix.org",
                    subtitle = null,
                    isPublic = true,
                    isMatrixOrg = true,
                )
            )
        }
    }

    @Test
    fun `present - user change and reset`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService())
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            sut.setAccountProvider(AccountProvider(url = "https://example.com"))
            val changedState = awaitItem()
            assertThat(changedState).isEqualTo(
                AccountProvider(
                    url = "https://example.com",
                    title = "example.com",
                    subtitle = null,
                    isPublic = false,
                    isMatrixOrg = false,
                )
            )
            sut.reset()
            val resetState = awaitItem()
            assertThat(resetState.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
        }
    }

    @Test
    fun `present - set url and reset`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService())
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            sut.setUrl(url = "https://example.com")
            val changedState = awaitItem()
            assertThat(changedState).isEqualTo(
                AccountProvider(
                    url = "https://example.com",
                    title = "example.com",
                    subtitle = null,
                    isPublic = false,
                    isMatrixOrg = false,
                )
            )
            sut.reset()
            val resetState = awaitItem()
            assertThat(resetState.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
        }
    }
}
