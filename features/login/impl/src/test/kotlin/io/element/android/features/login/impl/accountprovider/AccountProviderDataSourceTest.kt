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
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.api.accountprovider.AccountProvider
import io.element.android.libraries.matrix.api.accountprovider.matrixOrgAccountProvider
import io.element.android.libraries.matrix.test.accountprovider.anAccountProviderManaged
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountProviderDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state - matrix org is the default when nothing is configured`() = runTest {
        val sut = anAccountProviderDataSource()
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(matrixOrgAccountProvider)
        }
    }

    @Test
    fun `present - initial state - the first configured account provider is the default`() = runTest {
        val sut = anAccountProviderDataSource(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = {
                    listOf(anAccountProviderManaged(serverName = "first.org"), anAccountProviderManaged(serverName = "second.org"))
                }
            ),
        )
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(anAccountProviderManaged(serverName = "first.org"))
        }
    }

    @Test
    fun `present - user change and reset`() = runTest {
        val sut = anAccountProviderDataSource()
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(matrixOrgAccountProvider)
            sut.setAccountProvider(AccountProvider.Generic("https://example.com"))
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://example.com"))
            sut.reset()
            assertThat(awaitItem()).isEqualTo(matrixOrgAccountProvider)
        }
    }

    @Test
    fun `present - set url and reset`() = runTest {
        val sut = anAccountProviderDataSource()
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(matrixOrgAccountProvider)
            sut.setUrl(url = "https://example.com")
            assertThat(awaitItem()).isEqualTo(
                AccountProvider.Generic("https://example.com")
            )
            sut.reset()
            assertThat(awaitItem()).isEqualTo(matrixOrgAccountProvider)
        }
    }

    @Test
    fun `present - a provider set by the user is stored as they input it`() = runTest {
        val sut = anAccountProviderDataSource()
        sut.flow.test {
            skipItems(1)
            sut.setUrl(url = "example.com")
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("example.com"))
        }
    }

    @Test
    fun `present - defaults to the most recently used provider from history`() = runTest {
        val sut = anAccountProviderDataSource(
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://example.com", "https://matrix.org"),
            ),
        )
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://example.com"))
        }
    }

    @Test
    fun `present - history is ignored when the account provider is enforced`() = runTest {
        val sut = anAccountProviderDataSource(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = "enforced.org")) },
                canConnectToAnyAccountProviderResult = { false },
            ),
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://example.com"),
            ),
        )
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(anAccountProviderManaged(serverName = "enforced.org"))
        }
    }

    @Test
    fun `present - history wins over the configured provider when other providers are allowed`() = runTest {
        val sut = anAccountProviderDataSource(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = "preferred.org")) },
                canConnectToAnyAccountProviderResult = { true },
            ),
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://example.com"),
            ),
        )
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://example.com"))
        }
    }

    @Test
    fun `present - reset returns to the most recently used provider from history`() = runTest {
        val sut = anAccountProviderDataSource(
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://example.com"),
            ),
        )
        sut.flow.test {
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://example.com"))
            sut.setUrl("https://other.com")
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://other.com"))
            sut.reset()
            assertThat(awaitItem()).isEqualTo(AccountProvider.Generic("https://example.com"))
        }
    }
}
