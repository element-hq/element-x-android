/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { emptyList() }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isTrue()
        }
    }

    @Test
    fun `present - fixed list of account providers`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = {
                    listOf(AN_ACCOUNT_PROVIDER, AN_ACCOUNT_PROVIDER_2)
                }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                    ),
                    AccountProvider(
                        url = "https://element.io",
                        title = "element.io",
                        subtitle = null,
                        isPublic = false,
                        isMatrixOrg = false,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isFalse()
        }
    }

    @Test
    fun `present - opened list of account providers`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = {
                    listOf(AN_ACCOUNT_PROVIDER, EnterpriseService.ANY_ACCOUNT_PROVIDER)
                }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isTrue()
        }
    }
}
